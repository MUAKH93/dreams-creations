package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.auth.ForgotPasswordResponse;
import com.dreams.dreamscreations.entity.PasswordResetToken;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.PasswordResetTokenRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final String GENERIC_MESSAGE =
            "If an account exists with that email, password reset instructions have been sent.";

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.password-reset.expose-link:true}")
    private boolean exposeLink;

    public PasswordResetServiceImpl(UserRepository userRepo,
                                    PasswordResetTokenRepository tokenRepo,
                                    PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ForgotPasswordResponse requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        var userOpt = userRepo.findAllByEmail(email.trim()).stream().findFirst();
        if (userOpt.isEmpty()) {
            return ForgotPasswordResponse.builder().message(GENERIC_MESSAGE).build();
        }

        User user = userOpt.get();
        tokenRepo.invalidateActiveTokensForUser(user, LocalDateTime.now());

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenRepo.save(PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build());

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        log.info("Password reset requested for user {} — link: {}", user.getUsername(), resetLink);

        ForgotPasswordResponse.ForgotPasswordResponseBuilder response = ForgotPasswordResponse.builder()
                .message(GENERIC_MESSAGE);
        if (exposeLink) {
            response.resetLink(resetLink);
        }
        return response.build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        return tokenRepo.findByTokenWithUser(token.trim())
                .filter(t -> !t.isUsed() && !t.isExpired())
                .isPresent();
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Reset token is required");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        PasswordResetToken resetToken = tokenRepo.findByTokenWithUser(token.trim())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset link has already been used");
        }
        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset link has expired — request a new one");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepo.save(resetToken);
        tokenRepo.invalidateActiveTokensForUser(user, LocalDateTime.now());
    }
}
