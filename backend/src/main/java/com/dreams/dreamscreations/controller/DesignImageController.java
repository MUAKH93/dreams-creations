package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.config.UploadStorage;
import com.dreams.dreamscreations.entity.DesignImage;
import com.dreams.dreamscreations.repository.DesignImageRepository;
import com.dreams.dreamscreations.service.DesignImageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/design-images")
public class DesignImageController {

    private final DesignImageService designImageService;
    private final UploadStorage uploadStorage;
    private final DesignImageRepository designImageRepository;

    public DesignImageController(DesignImageService designImageService,
                                 UploadStorage uploadStorage,
                                 DesignImageRepository designImageRepository) {
        this.designImageService = designImageService;
        this.uploadStorage = uploadStorage;
        this.designImageRepository = designImageRepository;
    }

    @PostMapping("/upload/{designId}")
    public ResponseEntity<DesignImage> uploadImage(
            @PathVariable Long designId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(designImageService.uploadImage(designId, file));
    }

    @GetMapping("/view/{imageName}")
    public ResponseEntity<byte[]> viewImage(@PathVariable String imageName) throws IOException {
        if (imageName.contains("..") || imageName.contains("/") || imageName.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path path = locateImageFile(imageName);
        if (path == null) {
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

    /** Try configured upload dir, DB-stored path, then legacy ~/dreams-creations/uploads. */
    private Path locateImageFile(String imageName) {
        Path configured = uploadStorage.resolve(imageName);
        if (Files.exists(configured)) {
            return configured;
        }

        Optional<DesignImage> record = designImageRepository.findByImageName(imageName);
        if (record.isPresent()) {
            String storedPath = record.get().getImagePath();
            if (storedPath != null && !storedPath.isBlank()) {
                Path fromDb = Paths.get(storedPath);
                if (Files.exists(fromDb)) {
                    return fromDb;
                }
            }
        }

        Path legacyHome = Paths.get(System.getProperty("user.home"), "dreams-creations", "uploads", imageName);
        if (Files.exists(legacyHome)) {
            return legacyHome;
        }

        Path legacyProjects = Paths.get(System.getProperty("user.home"), "Projects", "dreams-creations", "backend", "uploads", imageName);
        if (Files.exists(legacyProjects)) {
            return legacyProjects;
        }

        return null;
    }

    @GetMapping("/design/{designId}")
    public List<DesignImage> getImagesByDesignId(@PathVariable Long designId) {
        return designImageService.getImagesByDesignId(designId);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        designImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{imageId}")
    public ResponseEntity<DesignImage> updateImage(
            @PathVariable Long imageId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(designImageService.updateImage(imageId, file));
    }
}
