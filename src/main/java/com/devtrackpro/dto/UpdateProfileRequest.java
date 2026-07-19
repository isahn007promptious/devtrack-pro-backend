package com.devtrackpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 25, message = "Cannot exceed 25 skills")
    private List<String> skills;

    @Pattern(regexp = "^(https?://.*)?$", message = "GitHub link must be a valid URL")
    @Size(max = 255)
    private String githubLink;

    @Pattern(regexp = "^(https?://.*)?$", message = "LinkedIn link must be a valid URL")
    @Size(max = 255)
    private String linkedinLink;
}
