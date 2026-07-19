package com.devtrackpro.service.impl;

import com.devtrackpro.dto.DashboardResponse;
import com.devtrackpro.entity.Project;
import com.devtrackpro.entity.Task;
import com.devtrackpro.entity.TaskStatus;
import com.devtrackpro.entity.User;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.ProjectRepository;
import com.devtrackpro.repository.TaskRepository;
import com.devtrackpro.repository.UserRepository;
import com.devtrackpro.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                ProjectRepository projectRepository,
                                TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch accessible projects
        List<Project> projects = projectRepository.findAllAccessibleProjects(user.getId());
        if (projects.isEmpty()) {
            return DashboardResponse.builder()
                    .totalProjects(0)
                    .totalTasks(0)
                    .completedTasks(0)
                    .pendingTasks(0)
                    .overdueTasks(0)
                    .productivity(Collections.emptyList())
                    .build();
        }

        List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        long totalProjects = projects.size();
        long totalTasks = taskRepository.countByProjectIdIn(projectIds);
        long completedTasks = taskRepository.countByProjectIdInAndStatus(projectIds, TaskStatus.DONE);
        long pendingTasks = taskRepository.countByProjectIdInAndStatusNot(projectIds, TaskStatus.DONE);
        long overdueTasks = taskRepository.countByProjectIdInAndStatusNotAndDueDateBefore(
                projectIds, TaskStatus.DONE, LocalDate.now()
        );

        // Fetch completed tasks in the last 4 weeks for productivity chart
        LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
        List<Task> completed = taskRepository.findByProjectIdInAndStatusAndUpdatedAtAfter(
                projectIds, TaskStatus.DONE, fourWeeksAgo
        );

        LocalDateTime now = LocalDateTime.now();
        List<DashboardResponse.ProductivityData> productivity = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            LocalDateTime start = now.minusWeeks(i + 1);
            LocalDateTime end = now.minusWeeks(i);
            long count = completed.stream()
                    .filter(t -> t.getUpdatedAt().isAfter(start) && t.getUpdatedAt().isBefore(end))
                    .count();
            String label = "Week " + (4 - i);
            productivity.add(new DashboardResponse.ProductivityData(label, count));
        }

        return DashboardResponse.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .productivity(productivity)
                .build();
    }
}
