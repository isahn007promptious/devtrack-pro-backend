package com.devtrackpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private UserSummaryResponse reporter;
    private UserSummaryResponse assignee;
    private Long parentTaskId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String type;
    private String taskKey;
    private Integer storyPoints;
    private Integer estimatedHours;
    private LocalDate dueDate;
    private List<LabelResponse> labels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
