package com.devtrackpro.service.impl;

import com.devtrackpro.dto.*;
import com.devtrackpro.entity.*;
import com.devtrackpro.exception.BadRequestException;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.*;
import com.devtrackpro.service.ActivityLogService;
import com.devtrackpro.service.NotificationService;
import com.devtrackpro.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           SprintRepository sprintRepository,
                           UserRepository userRepository,
                           LabelRepository labelRepository,
                           ProjectRepository projectRepository,
                           NotificationService notificationService,
                           ActivityLogService activityLogService) {
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Override
    public TaskResponse createTask(Long sprintId, TaskRequest request) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        Project project = sprint.getProject();
        User reporter = getAuthenticatedUser();

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        // Generate Task Key: PREFIX - (Count + 1)
        long nextIndex = taskRepository.countByProjectId(project.getId()) + 1;
        String taskKey = project.getKeyPrefix() + "-" + nextIndex;

        Set<Label> labels = new HashSet<>();
        if (request.getLabels() != null) {
            for (String labelName : request.getLabels()) {
                Label label = labelRepository.findByName(labelName)
                        .orElseGet(() -> labelRepository.save(
                                Label.builder()
                                        .name(labelName)
                                        .color("#3b82f6") // Default blue
                                        .build()
                        ));
                labels.add(label);
            }
        }

        Task task = Task.builder()
                .project(project)
                .sprint(sprint)
                .reporter(reporter)
                .assignee(assignee)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.valueOf(request.getStatus()))
                .priority(TaskPriority.valueOf(request.getPriority()))
                .type(TaskType.valueOf(request.getType()))
                .taskKey(taskKey)
                .storyPoints(request.getStoryPoints())
                .estimatedHours(request.getEstimatedHours())
                .dueDate(request.getDueDate())
                .labels(labels)
                .build();

        Task saved = taskRepository.save(task);

        // Log Activity
        activityLogService.logActivity(
                reporter.getId(),
                "CREATE_TASK",
                project.getId(),
                saved.getId(),
                reporter.getFirstName() + " " + reporter.getLastName() + " created Task " + saved.getTaskKey() + ": " + saved.getTitle()
        );

        // Notify Assignee
        if (assignee != null) {
            notificationService.notifyUser(
                    assignee.getId(),
                    "TASK_ASSIGNED",
                    reporter.getFirstName() + " " + reporter.getLastName() + " assigned you the task: " + saved.getTaskKey(),
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return mapToResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User currentUser = getAuthenticatedUser();
        User oldAssignee = task.getAssignee();

        User newAssignee = null;
        if (request.getAssigneeId() != null) {
            newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        // Update basic fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignee(newAssignee);
        task.setDueDate(request.getDueDate());
        task.setStoryPoints(request.getStoryPoints());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setPriority(TaskPriority.valueOf(request.getPriority()));
        task.setType(TaskType.valueOf(request.getType()));
        task.setStatus(TaskStatus.valueOf(request.getStatus()));

        // Labels mapping
        Set<Label> labels = new HashSet<>();
        if (request.getLabels() != null) {
            for (String labelName : request.getLabels()) {
                Label label = labelRepository.findByName(labelName)
                        .orElseGet(() -> labelRepository.save(
                                Label.builder()
                                        .name(labelName)
                                        .color("#3b82f6")
                                        .build()
                        ));
                labels.add(label);
            }
        }
        task.setLabels(labels);

        Task saved = taskRepository.save(task);

        // Log Activity
        activityLogService.logActivity(
                currentUser.getId(),
                "UPDATE_TASK",
                saved.getProject().getId(),
                saved.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName() + " updated details for " + saved.getTaskKey()
        );

        // Notify if assignee changed
        if (newAssignee != null && (oldAssignee == null || !oldAssignee.getId().equals(newAssignee.getId()))) {
            notificationService.notifyUser(
                    newAssignee.getId(),
                    "TASK_ASSIGNED",
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " assigned you the task: " + saved.getTaskKey(),
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse patchTaskStatus(Long id, String statusStr) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        TaskStatus newStatus = TaskStatus.valueOf(statusStr);
        TaskStatus oldStatus = task.getStatus();

        if (oldStatus == newStatus) {
            return mapToResponse(task);
        }

        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);

        User currentUser = getAuthenticatedUser();

        // Log Activity
        activityLogService.logActivity(
                currentUser.getId(),
                "PATCH_STATUS",
                saved.getProject().getId(),
                saved.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName() + " moved " + saved.getTaskKey() + " from " + oldStatus + " to " + newStatus
        );

        if (newStatus == TaskStatus.DONE) {
            activityLogService.logActivity(
                    currentUser.getId(),
                    "COMPLETE_TASK",
                    saved.getProject().getId(),
                    saved.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " completed " + saved.getTaskKey()
            );
        }

        return mapToResponse(saved);
    }

    @Override
    public TaskResponse patchTaskAssignee(Long id, Long assigneeId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        task.setAssignee(assignee);
        Task saved = taskRepository.save(task);

        User currentUser = getAuthenticatedUser();

        if (assignee != null) {
            activityLogService.logActivity(
                    currentUser.getId(),
                    "ASSIGN_TASK",
                    saved.getProject().getId(),
                    saved.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " assigned " + saved.getTaskKey() + " to " + assignee.getFirstName() + " " + assignee.getLastName()
            );

            // Notify
            notificationService.notifyUser(
                    assignee.getId(),
                    "TASK_ASSIGNED",
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " assigned you the task: " + saved.getTaskKey(),
                    saved.getId()
            );
        } else {
            activityLogService.logActivity(
                    currentUser.getId(),
                    "UNASSIGN_TASK",
                    saved.getProject().getId(),
                    saved.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " unassigned " + saved.getTaskKey()
            );
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KanbanBoardResponse getKanbanBoard(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<TaskResponse> taskResponses = tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<TaskResponse> todo = new ArrayList<>();
        List<TaskResponse> inProgress = new ArrayList<>();
        List<TaskResponse> review = new ArrayList<>();
        List<TaskResponse> done = new ArrayList<>();

        for (TaskResponse t : taskResponses) {
            switch (t.getStatus()) {
                case "TODO": todo.add(t); break;
                case "IN_PROGRESS": inProgress.add(t); break;
                case "REVIEW": review.add(t); break;
                case "DONE": done.add(t); break;
            }
        }

        return KanbanBoardResponse.builder()
                .todo(todo)
                .inProgress(inProgress)
                .review(review)
                .done(done)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getSprintTasks(Long sprintId, Pageable pageable) {
        if (!sprintRepository.existsById(sprintId)) {
            throw new ResourceNotFoundException("Sprint not found");
        }

        return taskRepository.findBySprintId(sprintId, pageable)
                .map(this::mapToResponse);
    }

    private TaskResponse mapToResponse(Task task) {
        UserSummaryResponse reporter = UserSummaryResponse.builder()
                .id(task.getReporter().getId())
                .username(task.getReporter().getUsername())
                .email(task.getReporter().getEmail())
                .firstName(task.getReporter().getFirstName())
                .lastName(task.getReporter().getLastName())
                .avatarUrl(task.getReporter().getAvatarUrl())
                .build();

        UserSummaryResponse assignee = null;
        if (task.getAssignee() != null) {
            assignee = UserSummaryResponse.builder()
                    .id(task.getAssignee().getId())
                    .username(task.getAssignee().getUsername())
                    .email(task.getAssignee().getEmail())
                    .firstName(task.getAssignee().getFirstName())
                    .lastName(task.getAssignee().getLastName())
                    .avatarUrl(task.getAssignee().getAvatarUrl())
                    .build();
        }

        List<LabelResponse> labels = task.getLabels().stream()
                .map(l -> LabelResponse.builder()
                        .id(l.getId())
                        .name(l.getName())
                        .color(l.getColor())
                        .build())
                .collect(Collectors.toList());

        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .reporter(reporter)
                .assignee(assignee)
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .type(task.getType().name())
                .taskKey(task.getTaskKey())
                .storyPoints(task.getStoryPoints())
                .estimatedHours(task.getEstimatedHours())
                .dueDate(task.getDueDate())
                .labels(labels)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.authentication.BadCredentialsException("User not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
