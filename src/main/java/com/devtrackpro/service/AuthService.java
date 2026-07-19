package com.devtrackpro.service;

import com.devtrackpro.dto.*;

public interface AuthService {
    void register(RegisterRequest request);
    void verifyEmail(String token);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(TokenRefreshRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
