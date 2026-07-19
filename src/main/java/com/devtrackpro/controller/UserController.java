package com.devtrackpro.controller;

import com.devtrackpro.dto.UpdateProfileRequest;
import com.devtrackpro.dto.UserProfileResponse;
import com.devtrackpro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing user profiles")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user's profile details")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
        UserProfileResponse response = userService.getCurrentUserProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current logged-in user's profile details")
    public ResponseEntity<UserProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request, Principal principal) {
        UserProfileResponse response = userService.updateCurrentUserProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }
}
