package com.devtrackpro.service.impl;

import com.devtrackpro.dto.*;
import com.devtrackpro.entity.*;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.*;
import com.devtrackpro.service.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;

    public SearchServiceImpl(UserRepository userRepository,
                             ProjectRepository projectRepository,
                             TaskRepository taskRepository,
                             LabelRepository labelRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String query, String currentUserEmail) {
        if (query == null || query.isBlank()) {
            return SearchResponse.builder()
                    .projects(Collections.emptyList())
                    .tasks(Collections.emptyList())
                    .users(Collections.emptyList())
                    .labels(Collections.emptyList())
                    .build();
        }

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Fetch accessible projects
        List<Project> accessibleProjects = projectRepository.findAllAccessibleProjects(user.getId());
        List<Long> projectIds = accessibleProjects.stream().map(Project::getId).collect(Collectors.toList());

        // 2. Filter projects matching query
        List<ProjectResponse> projects = accessibleProjects.stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) || 
                             p.getKeyPrefix().toLowerCase().contains(query.toLowerCase()))
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return SearchResponse.builder()
                    .projects(projects)
                    .tasks(Collections.emptyList())
                    .users(Collections.emptyList())
                    .labels(Collections.emptyList())
                    .build();
        }

        // 3. Search tasks in accessible projects
        List<TaskResponse> tasks = taskRepository.searchTasksInProjects(projectIds, query).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());

        // 4. Search users in current user's organizations
        List<UserSummaryResponse> users = userRepository.searchUsersInMyOrganizations(user.getId(), query).stream()
                .map(u -> UserSummaryResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .avatarUrl(u.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());

        // 5. Search labels matching query
        List<LabelResponse> labels = labelRepository.findByNameContainingIgnoreCase(query).stream()
                .map(l -> LabelResponse.builder()
                        .id(l.getId())
                        .name(l.getName())
                        .color(l.getColor())
                        .build())
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .projects(projects)
                .tasks(tasks)
                .users(users)
                .labels(labels)
                .build();
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        long totalTasks = taskRepository.countByProjectId(project.getId());
        Double progress = 0.0;
        if (totalTasks > 0) {
            long doneTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
            progress = ((double) doneTasks / totalTasks) * 100.0;
        }

        List<UserSummaryResponse> members = project.getMembers().stream()
                .map(m -> UserSummaryResponse.builder()
                        .id(m.getId())
                        .username(m.getUsername())
                        .email(m.getEmail())
                        .firstName(m.getFirstName())
                        .lastName(m.getLastName())
                        .avatarUrl(m.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());

        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .name(project.getName())
                .keyPrefix(project.getKeyPrefix())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .priority(project.getPriority().name())
                .progress(progress)
                .isArchived(project.isArchived())
                .members(members)
                .createdAt(project.getCreatedAt())
                .build();
    }

    private TaskResponse mapToTaskResponse(Task task) {
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
}
