package com.devtrackpro.controller;

import com.devtrackpro.dto.*;
import com.devtrackpro.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Tasks", description = "Endpoints for managing workspace project tasks, Kanban states, and assignments")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/sprints/{sprintId}/tasks")
    @PreAuthorize("@security.isSprintMember(#sprintId)")
    @Operation(summary = "Create a task inside a sprint")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long sprintId,
                                                   @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(sprintId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sprints/{sprintId}/tasks")
    @PreAuthorize("@security.isSprintMember(#sprintId)")
    @Operation(summary = "List paginated tasks under a sprint (Supports sorting by priority, due date, status)")
    public ResponseEntity<Page<TaskResponse>> getSprintTasks(
            @PathVariable Long sprintId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "priority") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TaskResponse> response = taskService.getSprintTasks(sprintId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{id}")
    @PreAuthorize("@security.isTaskMember(#id)")
    @Operation(summary = "Get task details by ID")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        TaskResponse response = taskService.getTask(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("@security.isTaskMember(#id)")
    @Operation(summary = "Update task details")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("@security.isTaskMember(#id)")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tasks/{id}/status")
    @PreAuthorize("@security.isTaskMember(#id)")
    @Operation(summary = "Move a task's status between Kanban columns")
    public ResponseEntity<TaskResponse> patchTaskStatus(@PathVariable Long id,
                                                        @Valid @RequestBody TaskStatusRequest request) {
        TaskResponse response = taskService.patchTaskStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{id}/assign")
    @PreAuthorize("@security.isTaskMember(#id)")
    @Operation(summary = "Assign a task to a user")
    public ResponseEntity<TaskResponse> patchTaskAssignee(@PathVariable Long id,
                                                          @RequestBody TaskAssigneeRequest request) {
        TaskResponse response = taskService.patchTaskAssignee(id, request.getAssigneeId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{id}/board")
    @PreAuthorize("@security.isProjectMember(#id)")
    @Operation(summary = "Get Kanban board grouping of tasks by status (TODO, IN_PROGRESS, REVIEW, DONE)")
    public ResponseEntity<KanbanBoardResponse> getKanbanBoard(@PathVariable Long id) {
        KanbanBoardResponse response = taskService.getKanbanBoard(id);
        return ResponseEntity.ok(response);
    }
}
