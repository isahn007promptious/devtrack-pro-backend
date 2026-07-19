package com.devtrackpro.dto;

import lombok.Data;

@Data
public class TaskAssigneeRequest {
    private Long assigneeId; // Nullable to allow unassigning
}
