package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.admin.CreateLoginForSupervisorRequest;
import com.dreams.dreamscreations.dto.admin.CreateSupervisorAccountRequest;
import com.dreams.dreamscreations.dto.admin.SupervisorAccountDTO;
import com.dreams.dreamscreations.entity.Role;
import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.RoleRepository;
import com.dreams.dreamscreations.repository.SupervisorRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.service.StaffAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StaffAccountServiceImpl implements StaffAccountService {

    private final SupervisorRepository supervisorRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public StaffAccountServiceImpl(SupervisorRepository supervisorRepo,
                                   UserRepository userRepo,
                                   RoleRepository roleRepo,
                                   PasswordEncoder passwordEncoder) {
        this.supervisorRepo = supervisorRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupervisorAccountDTO> listSupervisorAccounts() {
        return supervisorRepo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SupervisorAccountDTO createSupervisorWithAccount(CreateSupervisorAccountRequest request) {
        validateNewAccount(request.getUsername(), request.getEmail(), request.getPassword());

        String normalizedEmail = requireText(request.getEmail(), "Email is required for portal login")
                .toLowerCase();

        supervisorRepo.findFirstByEmail(normalizedEmail).ifPresent(existing -> {
            if (findLinkedUser(existing).isPresent()) {
                throw new RuntimeException("A supervisor with email " + normalizedEmail + " already has a login");
            }
        });

        Supervisor supervisor = supervisorRepo.findFirstByEmail(normalizedEmail)
                .orElseGet(() -> supervisorRepo.save(Supervisor.builder()
                        .firstName(requireText(request.getFirstName(), "First name is required"))
                        .lastName(request.getLastName())
                        .phone(request.getPhone())
                        .email(normalizedEmail)
                        .hireDate(LocalDate.now())
                        .status("active")
                        .build()));

        User user = createSupervisorUser(
                request.getUsername(),
                normalizedEmail,
                request.getPassword());

        return toDto(supervisor, Optional.of(user));
    }

    @Override
    @Transactional
    public SupervisorAccountDTO createLoginForSupervisor(Long supervisorId,
                                                       CreateLoginForSupervisorRequest request) {
        Supervisor supervisor = supervisorRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + supervisorId));

        if (findLinkedUser(supervisor).isPresent()) {
            throw new RuntimeException("Supervisor already has a login account");
        }

        String email = request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail().trim()
                : supervisor.getEmail();

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Supervisor needs an email before creating a login");
        }

        validateNewAccount(request.getUsername(), email, request.getPassword());

        supervisor.setEmail(email.trim().toLowerCase());
        supervisorRepo.save(supervisor);

        User user = createSupervisorUser(request.getUsername(), email, request.getPassword());
        return toDto(supervisor, Optional.of(user));
    }

    private User createSupervisorUser(String username, String email, String password) {
        Role supervisorRole = roleRepo.findFirstByRoleName("SUPERVISOR")
                .orElseThrow(() -> new RuntimeException("SUPERVISOR role not found — run role seed SQL"));

        return userRepo.save(User.builder()
                .username(username.trim())
                .password(passwordEncoder.encode(password))
                .email(email.trim().toLowerCase())
                .role(supervisorRole)
                .status(true)
                .build());
    }

    private void validateNewAccount(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (userRepo.usernameTaken(username.trim())) {
            throw new RuntimeException("Username already taken: " + username);
        }

        String normalizedEmail = email.trim().toLowerCase();
        var existingUsers = userRepo.findAllByEmailWithRole(normalizedEmail);
        if (!existingUsers.isEmpty()) {
            boolean hasSupervisor = existingUsers.stream()
                    .anyMatch(u -> u.getRole() != null && "SUPERVISOR".equals(u.getRole().getRoleName()));
            if (hasSupervisor) {
                throw new RuntimeException("A supervisor login already exists for email: " + email);
            }
            throw new RuntimeException(
                    "Email already registered to another account (" + existingUsers.get(0).getUsername()
                    + "). Use a different email or remove the duplicate user record.");
        }
    }

    private Optional<User> findLinkedUser(Supervisor supervisor) {
        if (supervisor.getEmail() == null || supervisor.getEmail().isBlank()) {
            return Optional.empty();
        }
        return userRepo.findSupervisorUserByEmail(supervisor.getEmail().trim().toLowerCase());
    }

    private SupervisorAccountDTO toDto(Supervisor supervisor) {
        return toDto(supervisor, findLinkedUser(supervisor));
    }

    private SupervisorAccountDTO toDto(Supervisor supervisor, Optional<User> user) {
        return SupervisorAccountDTO.builder()
                .supervisorId(supervisor.getSupervisorId())
                .firstName(supervisor.getFirstName())
                .lastName(supervisor.getLastName())
                .phone(supervisor.getPhone())
                .email(supervisor.getEmail())
                .status(supervisor.getStatus())
                .hasLogin(user.isPresent())
                .userId(user.map(User::getUserId).orElse(null))
                .username(user.map(User::getUsername).orElse(null))
                .build();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }
}
