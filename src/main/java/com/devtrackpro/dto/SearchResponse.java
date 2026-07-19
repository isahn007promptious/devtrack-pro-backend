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
public class SearchResponse {
    private List<ProjectResponse> projects;
    private List<TaskResponse> tasks;
    private List<UserSummaryResponse> users;
    private List<LabelResponse> labels;
}
