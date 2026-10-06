package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.config.PlatformProperties;
import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.security.JwtUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlatformAuthService {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private final PlatformProperties platformProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PlatformAuthService(PlatformProperties platformProperties,
                               PasswordEncoder passwordEncoder,
                               JwtUtil jwtUtil) {
        this.platformProperties = platformProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        PlatformProperties.SuperAdmin cfg = platformProperties.getSuperAdmin();
        if (!cfg.isEnabled()) {
            throw new RuntimeException("Platform super-admin login is disabled");
        }
        if (isBlank(cfg.getPassword())) {
            throw new RuntimeException("Platform super-admin password is not configured");
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        if (!cfg.getUsername().equals(username)) {
            throw new RuntimeException("Invalid platform credentials");
        }
        if (!passwordMatches(request.getPassword(), cfg.getPassword())) {
            throw new RuntimeException("Invalid platform credentials");
        }

        UserDetails details = User.builder()
                .username(cfg.getUsername())
                .password("")
                .authorities(new SimpleGrantedAuthority("ROLE_" + ROLE_SUPER_ADMIN))
                .build();

        String token = jwtUtil.generateToken(details, ROLE_SUPER_ADMIN, null, null);

        return LoginResponse.builder()
                .token(token)
                .username(cfg.getUsername())
                .role(ROLE_SUPER_ADMIN)
                .userId(null)
                .tenantId(null)
                .build();
    }

    private boolean passwordMatches(String raw, String configured) {
        if (raw == null) {
            return false;
        }
        if (configured.startsWith("{bcrypt}") || configured.startsWith("$2a$") || configured.startsWith("$2b$")) {
            return passwordEncoder.matches(raw, configured.replace("{bcrypt}", ""));
        }
        return configured.equals(raw);
    }

    public boolean isPlatformSuperAdmin(String username) {
        return platformProperties.getSuperAdmin().getUsername().equals(username);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
