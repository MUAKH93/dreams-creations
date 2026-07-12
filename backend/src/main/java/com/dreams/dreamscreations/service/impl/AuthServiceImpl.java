package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.auth.LoginRequest;
import com.dreams.dreamscreations.dto.auth.LoginResponse;
import com.dreams.dreamscreations.dto.auth.RegisterRequest;
import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.Role;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.RoleRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.security.JwtUtil;
import com.dreams.dreamscreations.security.UserDetailsServiceImpl;
import com.dreams.dreamscreations.service.CustomerService;
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

    public AuthServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authManager,
                           JwtUtil jwtUtil,
                           UserDetailsServiceImpl userDetailsService,
                           CustomerService customerService,
                           CurrentUserService currentUserService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.customerService = customerService;
        this.currentUserService = currentUserService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = resolveLoginIdentifier(request.getUsername());

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        User user = userRepo.findFirstByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String role = user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER";
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

    public LoginResponse register(RegisterRequest request) {
        if (userRepo.usernameTaken(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }
        if (userRepo.emailTaken(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        Role customerRole = roleRepo.findFirstByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException(
                        "CUSTOMER role not found — run the role seed SQL"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(customerRole)
                .status(true)
                .build();
        userRepo.save(user);

        customerService.findByEmail(request.getEmail()).orElseGet(() ->
                customerService.save(Customer.builder()
                        .firstName(request.getUsername())
                        .email(request.getEmail())
                        .status("active")
                        .build()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails, "CUSTOMER", user.getUserId());
        return buildLoginResponse(token, user, "CUSTOMER");
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
