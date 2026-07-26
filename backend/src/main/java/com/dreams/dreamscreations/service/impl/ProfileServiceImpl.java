package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.config.UploadStorage;
import com.dreams.dreamscreations.dto.UpdateProfileRequest;
import com.dreams.dreamscreations.dto.UserProfileDTO;
import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.CustomerRepository;
import com.dreams.dreamscreations.repository.SupervisorRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.EmailVerificationService;
import com.dreams.dreamscreations.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final SupervisorRepository supervisorRepo;
    private final CurrentUserService currentUserService;
    private final UploadStorage uploadStorage;
    private final EmailVerificationService emailVerificationService;

    public ProfileServiceImpl(UserRepository userRepo,
                              CustomerRepository customerRepo,
                              SupervisorRepository supervisorRepo,
                              CurrentUserService currentUserService,
                              UploadStorage uploadStorage,
                              EmailVerificationService emailVerificationService) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.supervisorRepo = supervisorRepo;
        this.currentUserService = currentUserService;
        this.uploadStorage = uploadStorage;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getMyProfile() {
        return toDto(currentUserService.getCurrentUser());
    }

    @Override
    @Transactional
    public UserProfileDTO updateMyProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        String role = roleName(user);

        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new RuntimeException("First name is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepo.emailTakenByOther(email, user.getUserId())) {
            throw new RuntimeException("Email already used by another account");
        }

        boolean emailChanged = user.getEmail() == null
                || !user.getEmail().equalsIgnoreCase(email);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        user.setPhone(request.getPhone().trim());
        user.setEmail(email);

        if (emailChanged && "CUSTOMER".equals(role)) {
            user.setEmailVerified(false);
            userRepo.save(user);
            emailVerificationService.sendVerificationForUser(user);
        } else {
            if (emailChanged) {
                user.setEmailVerified(true);
            }
            userRepo.save(user);
        }

        syncLinkedProfile(user, request, role);
        return toDto(userRepo.findById(user.getUserId()).orElse(user));
    }

    @Override
    @Transactional
    public UserProfileDTO uploadPhoto(MultipartFile file) {
        User user = currentUserService.getCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Photo file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        String ext = switch (contentType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        String fileName = "profile-" + user.getUserId() + "-" + UUID.randomUUID() + "." + ext;
        String storedPath = "profiles/" + fileName;

        try {
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
                uploadStorage.deleteIfExists(user.getProfilePhoto());
            }
            uploadStorage.save(file.getInputStream(), storedPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile photo: " + e.getMessage());
        }

        user.setProfilePhoto(storedPath);
        userRepo.save(user);
        return toDto(user);
    }

    private void syncLinkedProfile(User user, UpdateProfileRequest request, String role) {
        if ("CUSTOMER".equals(role)) {
            Customer customer = customerRepo.findFirstByEmail(user.getEmail())
                    .orElseGet(() -> customerRepo.save(Customer.builder()
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phone(user.getPhone())
                            .email(user.getEmail())
                            .status("active")
                            .build()));
            customer.setFirstName(user.getFirstName());
            customer.setLastName(user.getLastName());
            customer.setPhone(user.getPhone());
            customer.setEmail(user.getEmail());
            if (request.getAddress() != null) customer.setAddress(request.getAddress().trim());
            if (request.getCity() != null) customer.setCity(request.getCity().trim());
            customerRepo.save(customer);
            return;
        }

        if ("SUPERVISOR".equals(role)) {
            supervisorRepo.findFirstByEmail(user.getEmail()).ifPresent(supervisor -> {
                supervisor.setFirstName(user.getFirstName());
                supervisor.setLastName(user.getLastName());
                supervisor.setPhone(user.getPhone());
                supervisor.setEmail(user.getEmail());
                supervisorRepo.save(supervisor);
            });
        }
    }

    private UserProfileDTO toDto(User user) {
        String role = roleName(user);
        Long customerId = currentUserService.resolveCustomerId(user);
        Long supervisorId = currentUserService.resolveSupervisorId(user);

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String phone = user.getPhone();
        String address = null;
        String city = null;

        if ("CUSTOMER".equals(role) && user.getEmail() != null) {
            Customer customer = customerRepo.findFirstByEmail(user.getEmail()).orElse(null);
            if (customer != null) {
                address = customer.getAddress();
                city = customer.getCity();
                if (firstName == null) firstName = customer.getFirstName();
                if (lastName == null) lastName = customer.getLastName();
                if (phone == null) phone = customer.getPhone();
            }
        }

        if ("SUPERVISOR".equals(role) && user.getEmail() != null) {
            Supervisor supervisor = supervisorRepo.findFirstByEmail(user.getEmail()).orElse(null);
            if (supervisor != null) {
                if (firstName == null) firstName = supervisor.getFirstName();
                if (lastName == null) lastName = supervisor.getLastName();
                if (phone == null) phone = supervisor.getPhone();
            }
        }

        boolean profileComplete = isNotBlank(user.getEmail())
                && isNotBlank(phone)
                && isNotBlank(firstName);

        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .profilePhotoUrl(buildPhotoUrl(user.getProfilePhoto()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .profileComplete(profileComplete)
                .customerId(customerId)
                .supervisorId(supervisorId)
                .address(address)
                .city(city)
                .build();
    }

    private String roleName(User user) {
        return user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER";
    }

    private String buildPhotoUrl(String profilePhoto) {
        if (profilePhoto == null || profilePhoto.isBlank()) return null;
        String fileName = profilePhoto.contains("/")
                ? profilePhoto.substring(profilePhoto.lastIndexOf('/') + 1)
                : profilePhoto;
        return "/api/profile/photos/view/" + fileName;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
