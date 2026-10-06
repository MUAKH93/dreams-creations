package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.platform.CreateTenantAdminRequest;
import com.dreams.dreamscreations.dto.platform.TenantAdminCreatedDTO;
import com.dreams.dreamscreations.entity.Role;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.RoleRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.tenant.TenantContext;
import com.dreams.dreamscreations.tenant.TenantRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformTenantAdminService {

    private final TenantRegistry tenantRegistry;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public PlatformTenantAdminService(TenantRegistry tenantRegistry,
                                      RoleRepository roleRepo,
                                      UserRepository userRepo,
                                      PasswordEncoder passwordEncoder) {
        this.tenantRegistry = tenantRegistry;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TenantAdminCreatedDTO createTenantAdmin(CreateTenantAdminRequest request) {
        validate(request);

        String previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(request.getTenantId().trim());

            Role adminRole = roleRepo.findFirstByRoleName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found in tenant database"));

            String username = request.getUsername().trim();
            if (userRepo.usernameTaken(username)) {
                throw new RuntimeException("Username already taken in this tenant: " + username);
            }

            String email = request.getEmail().trim().toLowerCase();
            if (userRepo.emailTaken(email)) {
                throw new RuntimeException("Email already registered in this tenant: " + email);
            }

            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(email)
                    .firstName(requireText(request.getFirstName(), "First name is required"))
                    .lastName(request.getLastName())
                    .role(adminRole)
                    .status(true)
                    .emailVerified(true)
                    .build();
            user = userRepo.save(user);

            return TenantAdminCreatedDTO.builder()
                    .tenantId(request.getTenantId().trim())
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role("ADMIN")
                    .build();
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void validate(CreateTenantAdminRequest request) {
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            throw new RuntimeException("tenantId is required");
        }
        if (!tenantRegistry.isValid(request.getTenantId().trim())) {
            throw new RuntimeException("Unknown tenant: " + request.getTenantId());
        }
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new RuntimeException("Valid email is required");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }
}
