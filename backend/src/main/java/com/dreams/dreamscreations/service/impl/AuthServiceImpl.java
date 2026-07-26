package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.dto.auth.RegisterRequest;
import com.dreams.dreamscreations.dto.auth.RegisterResponse;
import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.Role;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.exception.EmailNotVerifiedException;
import com.dreams.dreamscreations.repository.RoleRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.security.JwtUtil;
import com.dreams.dreamscreations.security.UserDetailsServiceImpl;
import com.dreams.dreamscreations.service.CustomerService;
import com.dreams.dreamscreations.service.EmailVerificationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomerService customerService;
    private final CurrentUserService currentUserService;
    private final EmailVerificationService emailVerificationService;

    public AuthServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authManager,
                           JwtUtil jwtUtil,
                           UserDetailsServiceImpl userDetailsService,
                           CustomerService customerService,
                           CurrentUserService currentUserService,
                           EmailVerificationService emailVerificationService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.customerService = customerService;
        this.currentUserService = currentUserService;
        this.emailVerificationService = emailVerificationService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = resolveLoginIdentifier(request.getUsername());

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        User user = userRepo.findFirstByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER";
        if ("CUSTOMER".equals(role) && !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before signing in. Check your inbox or request a new verification link.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails, role, user.getUserId());

        return buildLoginResponse(token, user, role);
    }

    private String resolveLoginIdentifier(String input) {
        if (input == null || input.isBlank()) {
            throw new RuntimeException("Username or email is required");
        }
        String trimmed = input.trim();
        if (trimmed.contains("@")) {
            return userRepo.findAllByEmail(trimmed).stream()
                    .findFirst()
                    .map(User::getUsername)
                    .orElse(trimmed);
        }
        return trimmed;
    }

    public RegisterResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepo.usernameTaken(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }
        if (userRepo.emailTaken(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        Role customerRole = roleRepo.findFirstByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException(
                        "CUSTOMER role not found — run the role seed SQL"));

        String email = request.getEmail().trim().toLowerCase();
        User user = User.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(email)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .phone(request.getPhone().trim())
                .role(customerRole)
                .status(true)
                .emailVerified(false)
                .build();
        userRepo.save(user);

        customerService.findByEmail(email).orElseGet(() ->
                customerService.save(Customer.builder()
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .email(email)
                        .status("active")
                        .build()));

        return emailVerificationService.sendVerificationForUser(user);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new RuntimeException("A valid email is required");
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new RuntimeException("First name is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }
    }

    private LoginResponse buildLoginResponse(String token, User user, String role) {
        Long customerId = currentUserService.resolveCustomerId(user);
        Long supervisorId = currentUserService.resolveSupervisorId(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(role)
                .userId(user.getUserId())
                .customerId(customerId)
                .supervisorId(supervisorId)
                .build();
    }
}
