package com.devtrackpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalProjects;
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;
    private List<ProductivityData> productivity;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductivityData {
        private String weekLabel;
        private long completedCount;
    }
}
