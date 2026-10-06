package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.service.PlatformAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/platform")
public class PlatformAuthController {

    private final PlatformAuthService platformAuthService;

    public PlatformAuthController(PlatformAuthService platformAuthService) {
        this.platformAuthService = platformAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(platformAuthService.login(request));
    }
}
