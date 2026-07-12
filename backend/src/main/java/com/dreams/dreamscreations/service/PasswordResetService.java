package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.auth.ForgotPasswordResponse;

public interface PasswordResetService {
    ForgotPasswordResponse requestReset(String email);
    boolean validateToken(String token);
    void resetPassword(String token, String newPassword);
}
