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
public class ProjectResponse {
    private Long id;
    private Long workspaceId;
    private String name;
    private String keyPrefix;
    private String description;
    private LocalDate deadline;
    private String priority;
    private Double progress; // Derived progress value (percent DONE)
    private boolean isArchived;
    private List<UserSummaryResponse> members;
    private LocalDateTime createdAt;
}
