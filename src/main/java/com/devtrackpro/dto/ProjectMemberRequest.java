package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProjectMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(ADD|REMOVE)$", message = "Action must be ADD or REMOVE")
    private String action;
}
