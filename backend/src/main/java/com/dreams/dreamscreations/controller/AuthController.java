package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.dto.auth.RegisterRequest;
import com.dreams.dreamscreations.service.impl.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
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
}
