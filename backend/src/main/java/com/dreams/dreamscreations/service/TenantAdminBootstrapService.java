package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.config.BootstrapProperties;
import com.dreams.dreamscreations.entity.Role;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.RoleRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class TenantAdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(TenantAdminBootstrapService.class);
    private static final Set<String> LEGACY_ADMIN_USERNAMES = Set.of("admin", "administrator");

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public TenantAdminBootstrapService(RoleRepository roleRepo,
                                       UserRepository userRepo,
                                       PasswordEncoder passwordEncoder) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void upsertTenantAdmin(BootstrapProperties.TenantAdminSeed seed) {
        Role adminRole = roleRepo.findFirstByRoleName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing — run role seed SQL"));

        String username = seed.getUsername().trim();
        String email = isBlank(seed.getEmail())
                ? (username.toLowerCase() + "@dreamscreations.local")
                : seed.getEmail().trim().toLowerCase();

        User user = userRepo.findFirstByUsername(username).orElse(null);
        if (user == null) {
            if (userRepo.emailTaken(email)) {
                email = username.toLowerCase() + "+" + System.currentTimeMillis() + "@dreamscreations.local";
            }
            user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(seed.getPassword()))
                    .email(email)
                    .firstName(seed.getFirstName())
                    .lastName(seed.getLastName())
                    .role(adminRole)
                    .status(true)
                    .emailVerified(true)
                    .build();
            userRepo.save(user);
        } else {
            user.setPassword(passwordEncoder.encode(seed.getPassword()));
            user.setRole(adminRole);
            user.setStatus(true);
            user.setEmailVerified(true);
            if (user.getFirstName() == null) user.setFirstName(seed.getFirstName());
            if (user.getLastName() == null) user.setLastName(seed.getLastName());
            userRepo.save(user);
        }

        if (seed.isDisableLegacyAdmins()) {
            disableLegacyAdmins(username);
        }
    }

    private void disableLegacyAdmins(String keepUsername) {
        List<User> admins = userRepo.findAllByRoleName("ADMIN");
        for (User u : admins) {
            if (u.getUsername().equalsIgnoreCase(keepUsername)) {
                continue;
            }
            if (LEGACY_ADMIN_USERNAMES.contains(u.getUsername().toLowerCase())) {
                u.setStatus(false);
                userRepo.save(u);
                log.info("Disabled legacy admin user '{}'", u.getUsername());
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
