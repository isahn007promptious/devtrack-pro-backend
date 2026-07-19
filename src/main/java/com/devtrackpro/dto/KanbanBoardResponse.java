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
public class KanbanBoardResponse {
    private List<TaskResponse> todo;
    private List<TaskResponse> inProgress;
    private List<TaskResponse> review;
    private List<TaskResponse> done;
}
