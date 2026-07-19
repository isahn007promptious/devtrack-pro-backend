package com.devtrackpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InviteRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(OWNER|ADMIN|PROJECT_MANAGER|DEVELOPER|VIEWER)$", 
             message = "Role must be one of: OWNER, ADMIN, PROJECT_MANAGER, DEVELOPER, VIEWER")
    private String role;
}
