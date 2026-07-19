package com.devtrackpro.service.impl;

import com.devtrackpro.entity.Task;
import com.devtrackpro.entity.TaskStatus;
import com.devtrackpro.repository.NotificationRepository;
import com.devtrackpro.repository.TaskRepository;
import com.devtrackpro.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TaskDeadlineChecker {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public TaskDeadlineChecker(TaskRepository taskRepository,
                               NotificationRepository notificationRepository,
                               NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    // Run every 5 minutes for local testing and checking (can also be a standard daily cron)
    @Scheduled(fixedDelay = 300000)
    public void checkApproachingDeadlines() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Task> approachingTasks = taskRepository.findByDueDateAndStatusNotAndAssigneeNotNull(
                tomorrow, TaskStatus.DONE
        );

        for (Task task : approachingTasks) {
            Long assigneeId = task.getAssignee().getId();
            
            // Check if notification already exists to prevent duplication
            boolean alreadyNotified = notificationRepository.existsByUserIdAndTypeAndRelatedEntityId(
                    assigneeId, "DEADLINE_NEAR", task.getId()
            );

            if (!alreadyNotified) {
                notificationService.notifyUser(
                        assigneeId,
                        "DEADLINE_NEAR",
                        "The task " + task.getTaskKey() + " is due within 24 hours: " + task.getTitle(),
                        task.getId()
                );
            }
        }
    }
}
