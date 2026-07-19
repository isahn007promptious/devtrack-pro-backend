package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SprintRequest {

    @NotBlank(message = "Sprint name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String goal;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "Sprint status is required")
    @Pattern(regexp = "^(PLANNED|ACTIVE|COMPLETED)$", message = "Status must be PLANNED, ACTIVE, or COMPLETED")
    private String status;
}
