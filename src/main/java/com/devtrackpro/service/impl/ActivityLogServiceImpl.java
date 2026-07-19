package com.devtrackpro.service.impl;

import com.devtrackpro.dto.ActivityLogResponse;
import com.devtrackpro.entity.ActivityLog;
import com.devtrackpro.entity.Project;
import com.devtrackpro.entity.Task;
import com.devtrackpro.entity.User;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.ActivityLogRepository;
import com.devtrackpro.repository.ProjectRepository;
import com.devtrackpro.repository.TaskRepository;
import com.devtrackpro.repository.UserRepository;
import com.devtrackpro.service.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository,
                                  UserRepository userRepository,
                                  ProjectRepository projectRepository,
                                  TaskRepository taskRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void logActivity(Long userId, String action, Long projectId, Long taskId, String details) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return;

        Task task = taskId != null ? taskRepository.findById(taskId).orElse(null) : null;

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .project(project)
                .task(task)
                .action(action)
                .details(details)
                .build();

        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getProjectActivity(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return activityLogRepository.findByProjectId(projectId, pageable)
                .map(log -> ActivityLogResponse.builder()
                        .id(log.getId())
                        .userId(log.getUser().getId())
                        .userFullname(log.getUser().getFirstName() + " " + log.getUser().getLastName())
                        .userAvatar(log.getUser().getAvatarUrl())
                        .action(log.getAction())
                        .details(log.getDetails())
                        .createdAt(log.getCreatedAt())
                        .build());
    }
}
