package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Key prefix is required")
    @Size(min = 2, max = 10, message = "Key prefix must be between 2 and 10 characters")
    private String keyPrefix;

    @Size(max = 1000)
    private String description;

    private LocalDate deadline;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "Priority must be LOW, MEDIUM, or HIGH")
    private String priority;
}
