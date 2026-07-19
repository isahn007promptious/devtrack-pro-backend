package com.devtrackpro.service;

import com.devtrackpro.dto.UpdateProfileRequest;
import com.devtrackpro.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(String email);
    UserProfileResponse updateCurrentUserProfile(String email, UpdateProfileRequest request);
}
