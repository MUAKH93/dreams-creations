package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.auth.ForgotPasswordRequest;
import com.dreams.dreamscreations.dto.auth.ForgotPasswordResponse;
import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.dto.auth.RegisterRequest;
import com.dreams.dreamscreations.dto.auth.RegisterResponse;
import com.dreams.dreamscreations.dto.auth.ResetPasswordRequest;
import com.dreams.dreamscreations.service.EmailVerificationService;
import com.dreams.dreamscreations.service.PasswordResetService;
import com.dreams.dreamscreations.service.impl.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthServiceImpl authService,
                          PasswordResetService passwordResetService,
                          EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully. You can sign in now."
        ));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<RegisterResponse> resendVerification(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(emailVerificationService.resend(request.getEmail()));
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
