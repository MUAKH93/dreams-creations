package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.auth.ForgotPasswordRequest;
import com.dreams.dreamscreations.dto.auth.ForgotPasswordResponse;
import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.dto.auth.RegisterRequest;
import com.dreams.dreamscreations.dto.auth.ResetPasswordRequest;
import com.dreams.dreamscreations.service.PasswordResetService;
import com.dreams.dreamscreations.service.impl.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public authentication endpoints — no JWT required to call these.
 * Configured as permitAll() in SecurityConfig.
 *
 * POST /api/auth/login    → returns JWT token for any user type
 * POST /api/auth/register → creates a CUSTOMER account, returns JWT token
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthServiceImpl authService,
                          PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Login for all user types (admin, manager, supervisor, customer).
     * Request:  { "username": "admin", "password": "admin123" }
     * Response: { "token": "eyJ...", "username": "admin", "role": "ADMIN", "userId": 1 }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Self-registration for customers only.
     * Request:  { "username": "ahmed", "password": "pass123", "email": "ahmed@gmail.com" }
     * Response: { "token": "eyJ...", "username": "ahmed", "role": "CUSTOMER", "userId": 5 }
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.requestReset(request.getEmail()));
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("valid", passwordResetService.validateToken(token)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully. You can sign in now."));
    }
}
