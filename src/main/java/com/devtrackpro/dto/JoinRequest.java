package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRequest {

    @NotBlank(message = "Invitation token is required")
    private String token;
}
