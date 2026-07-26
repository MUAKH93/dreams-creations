package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.config.UploadStorage;
import com.dreams.dreamscreations.dto.UpdateProfileRequest;
import com.dreams.dreamscreations.dto.UserProfileDTO;
import com.dreams.dreamscreations.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UploadStorage uploadStorage;

    public ProfileController(ProfileService profileService, UploadStorage uploadStorage) {
        this.profileService = profileService;
        this.uploadStorage = uploadStorage;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateMyProfile(request));
    }

    @PostMapping("/me/photo")
    public ResponseEntity<UserProfileDTO> uploadPhoto(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadPhoto(file));
    }

    @GetMapping("/photos/view/{imageName}")
    public ResponseEntity<byte[]> viewPhoto(@PathVariable String imageName) throws IOException {
        if (imageName.contains("..") || imageName.contains("/") || imageName.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path path = uploadStorage.resolve("profiles/" + imageName);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(Files.readAllBytes(path));
    }

    @GetMapping("/me/photo-url")
    public ResponseEntity<Map<String, String>> photoUrl() {
        UserProfileDTO profile = profileService.getMyProfile();
        return ResponseEntity.ok(Map.of(
                "profilePhotoUrl", profile.getProfilePhotoUrl() != null ? profile.getProfilePhotoUrl() : ""
        ));
    }
}
