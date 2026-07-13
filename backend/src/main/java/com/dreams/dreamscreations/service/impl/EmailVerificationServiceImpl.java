package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.auth.RegisterResponse;
import com.dreams.dreamscreations.entity.EmailVerificationToken;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.EmailVerificationTokenRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.service.EmailService;
import com.dreams.dreamscreations.service.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private static final String GENERIC_RESEND_MESSAGE =
            "If an unverified account exists with that email, a new verification link has been sent.";

    private final UserRepository userRepo;
    private final EmailVerificationTokenRepository tokenRepo;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email-verification.expose-link:true}")
    private boolean exposeLink;

    public EmailVerificationServiceImpl(UserRepository userRepo,
                                        EmailVerificationTokenRepository tokenRepo,
                                        EmailService emailService) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public RegisterResponse sendVerificationForUser(User user) {
        tokenRepo.invalidateActiveTokensForUser(user, LocalDateTime.now());

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenRepo.save(EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        String verifyLink = frontendUrl + "/verify-email?token=" + token;
        String body = "Welcome to Dreams Creations!\n\n"
                + "Please verify your email address by opening this link (valid 24 hours):\n"
                + verifyLink + "\n\n"
                + "If you did not create this account, you can ignore this email.";

        emailService.send(user.getEmail(), "Verify your Dreams Creations account", body);
        log.info("Verification email for user {} — link: {}", user.getUsername(), verifyLink);

        RegisterResponse.RegisterResponseBuilder response = RegisterResponse.builder()
                .message("Account created. Please check your email to verify your account before signing in.")
                .email(user.getEmail())
                .emailVerificationRequired(true);
        if (exposeLink) {
            response.verificationLink(verifyLink);
        }
        return response.build();
    }

    @Override
    @Transactional
    public void verify(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Verification token is required");
        }

        EmailVerificationToken verificationToken = tokenRepo.findByTokenWithUser(token.trim())
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification link"));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("This verification link has already been used");
        }
        if (verificationToken.isExpired()) {
            throw new RuntimeException("This verification link has expired — request a new one");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepo.save(verificationToken);
        tokenRepo.invalidateActiveTokensForUser(user, LocalDateTime.now());
    }

    @Override
    @Transactional
    public RegisterResponse resend(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        var userOpt = userRepo.findAllByEmail(email.trim()).stream().findFirst();
        if (userOpt.isEmpty() || Boolean.TRUE.equals(userOpt.get().getEmailVerified())) {
            return RegisterResponse.builder()
                    .message(GENERIC_RESEND_MESSAGE)
                    .emailVerificationRequired(true)
                    .build();
        }

        return sendVerificationForUser(userOpt.get());
    }
}
