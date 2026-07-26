package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.admin.*;
import com.dreams.dreamscreations.entity.Role;
import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.ModuleAssignmentRepository;
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
    private final ModuleAssignmentRepository assignmentRepo;
    private final PasswordEncoder passwordEncoder;

    public StaffAccountServiceImpl(SupervisorRepository supervisorRepo,
                                   UserRepository userRepo,
                                   RoleRepository roleRepo,
                                   ModuleAssignmentRepository assignmentRepo,
                                   PasswordEncoder passwordEncoder) {
        this.supervisorRepo = supervisorRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.assignmentRepo = assignmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupervisorAccountDTO> listSupervisorAccounts() {
        return supervisorRepo.findAll().stream()
                .map(this::toSupervisorDto)
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

        User user = createSupervisorUser(request.getUsername(), normalizedEmail, request.getPassword());
        return toSupervisorDto(supervisor, Optional.of(user));
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
        return toSupervisorDto(supervisor, Optional.of(user));
    }

    @Override
    @Transactional
    public SupervisorAccountDTO updateSupervisor(Long supervisorId, UpdateSupervisorAccountRequest request) {
        Supervisor supervisor = supervisorRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + supervisorId));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            supervisor.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            supervisor.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            supervisor.setPhone(request.getPhone().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            supervisor.setStatus(request.getStatus().trim().toLowerCase());
        }

        Optional<User> linkedUser = findLinkedUser(supervisor);
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            linkedUser.ifPresent(user -> {
                if (userRepo.emailTakenByOther(newEmail, user.getUserId())) {
                    throw new RuntimeException("Email already used by another account: " + newEmail);
                }
                user.setEmail(newEmail);
                userRepo.save(user);
            });
            supervisor.setEmail(newEmail);
        }

        supervisorRepo.save(supervisor);
        return toSupervisorDto(supervisor, findLinkedUser(supervisor));
    }

    @Override
    @Transactional
    public SupervisorAccountDTO updateSupervisorLogin(Long supervisorId, UpdateStaffLoginRequest request) {
        Supervisor supervisor = supervisorRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + supervisorId));

        User user = findLinkedUser(supervisor)
                .orElseThrow(() -> new RuntimeException("Supervisor has no login account to update"));

        applyLoginUpdates(user, request);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            supervisor.setEmail(newEmail);
            supervisorRepo.save(supervisor);
        }

        userRepo.save(user);
        return toSupervisorDto(supervisor, Optional.of(user));
    }

    @Override
    @Transactional
    public void deleteSupervisor(Long supervisorId, Long actingAdminUserId) {
        Supervisor supervisor = supervisorRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + supervisorId));

        if (assignmentRepo.countBySupervisor_SupervisorId(supervisorId) > 0) {
            throw new RuntimeException(
                    "Cannot delete supervisor with production assignments. Set status to inactive instead.");
        }

        findLinkedUser(supervisor).ifPresent(user -> {
            if (user.getUserId().equals(actingAdminUserId)) {
                throw new RuntimeException("You cannot delete your own account");
            }
            userRepo.delete(user);
        });

        supervisorRepo.delete(supervisor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagerAccountDTO> listManagerAccounts() {
        return userRepo.findAllByRoleName("MANAGER").stream()
                .map(this::toManagerDto)
                .toList();
    }

    @Override
    @Transactional
    public ManagerAccountDTO createManagerAccount(CreateManagerAccountRequest request) {
        validateNewAccount(request.getUsername(), request.getEmail(), request.getPassword());

        Role managerRole = roleRepo.findFirstByRoleName("MANAGER")
                .orElseThrow(() -> new RuntimeException("MANAGER role not found — run role seed SQL"));

        User user = userRepo.save(User.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail().trim().toLowerCase())
                .role(managerRole)
                .status(true)
                .emailVerified(true)
                .build());

        return toManagerDto(user);
    }

    @Override
    @Transactional
    public ManagerAccountDTO updateManagerAccount(Long userId,
                                                  UpdateManagerAccountRequest request,
                                                  Long actingAdminUserId) {
        User user = userRepo.findManagerById(userId)
                .orElseThrow(() -> new RuntimeException("Manager account not found: " + userId));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String username = request.getUsername().trim();
            if (userRepo.usernameTakenByOther(username, userId)) {
                throw new RuntimeException("Username already taken: " + username);
            }
            user.setUsername(username);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (userRepo.emailTakenByOther(email, userId)) {
                throw new RuntimeException("Email already used: " + email);
            }
            user.setEmail(email);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getLoginEnabled() != null) {
            user.setStatus(request.getLoginEnabled());
        }

        return toManagerDto(userRepo.save(user));
    }

    @Override
    @Transactional
    public void deleteManagerAccount(Long userId, Long actingAdminUserId) {
        if (userId.equals(actingAdminUserId)) {
            throw new RuntimeException("You cannot delete your own account");
        }
        User user = userRepo.findManagerById(userId)
                .orElseThrow(() -> new RuntimeException("Manager account not found: " + userId));
        userRepo.delete(user);
    }

    @Override
    @Transactional
    public void resetStaffPassword(Long userId, String newPassword, Long actingAdminUserId) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User account not found: " + userId));
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        if ("CUSTOMER".equalsIgnoreCase(roleName)) {
            throw new RuntimeException("Customer passwords cannot be reset here — use forgot password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
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
                .emailVerified(true)
                .build());
    }

    private void applyLoginUpdates(User user, UpdateStaffLoginRequest request) {
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String username = request.getUsername().trim();
            if (userRepo.usernameTakenByOther(username, user.getUserId())) {
                throw new RuntimeException("Username already taken: " + username);
            }
            user.setUsername(username);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (userRepo.emailTakenByOther(email, user.getUserId())) {
                throw new RuntimeException("Email already used: " + email);
            }
            user.setEmail(email);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getLoginEnabled() != null) {
            user.setStatus(request.getLoginEnabled());
        }
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
        if (!userRepo.findAllByEmailWithRole(normalizedEmail).isEmpty()) {
            throw new RuntimeException("Email already registered: " + email);
        }
    }

    private Optional<User> findLinkedUser(Supervisor supervisor) {
        if (supervisor.getEmail() == null || supervisor.getEmail().isBlank()) {
            return Optional.empty();
        }
        return userRepo.findSupervisorUserByEmail(supervisor.getEmail().trim().toLowerCase());
    }

    private SupervisorAccountDTO toSupervisorDto(Supervisor supervisor) {
        return toSupervisorDto(supervisor, findLinkedUser(supervisor));
    }

    private SupervisorAccountDTO toSupervisorDto(Supervisor supervisor, Optional<User> user) {
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
                .loginEnabled(user.map(User::getStatus).orElse(null))
                .build();
    }

    private ManagerAccountDTO toManagerDto(User user) {
        return ManagerAccountDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .loginEnabled(user.getStatus())
                .build();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }
}
