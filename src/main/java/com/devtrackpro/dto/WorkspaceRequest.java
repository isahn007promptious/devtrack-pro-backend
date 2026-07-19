package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    private String slug;

    @Size(max = 1000)
    private String description;
}
