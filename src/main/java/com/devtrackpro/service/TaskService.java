package com.devtrackpro.service;

import com.devtrackpro.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(Long sprintId, TaskRequest request);
    TaskResponse getTask(Long id);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
    TaskResponse patchTaskStatus(Long id, String status);
    TaskResponse patchTaskAssignee(Long id, Long assigneeId);
    KanbanBoardResponse getKanbanBoard(Long projectId);
    Page<TaskResponse> getSprintTasks(Long sprintId, Pageable pageable);
}
