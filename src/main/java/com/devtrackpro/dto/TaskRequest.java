package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @Size(max = 2000)
    private String description;

    private Long assigneeId;

    private LocalDate dueDate;

    private Integer storyPoints;

    private Integer estimatedHours;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "Priority must be LOW, MEDIUM, HIGH, or CRITICAL")
    private String priority;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(STORY|TASK|BUG|EPIC)$", message = "Type must be STORY, TASK, BUG, or EPIC")
    private String type;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(TODO|IN_PROGRESS|REVIEW|DONE)$", message = "Status must be TODO, IN_PROGRESS, REVIEW, or DONE")
    private String status;

    private List<String> labels; // names of tags
}
