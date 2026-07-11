package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.config.UploadStorage;
import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import com.dreams.dreamscreations.repository.DesignImageRepository;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.service.DesignImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DesignImageServiceImpl implements DesignImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final DesignRepository designRepository;
    private final DesignImageRepository designImageRepository;
    private final UploadStorage uploadStorage;

    public DesignImageServiceImpl(DesignRepository designRepository,
                                  DesignImageRepository designImageRepository,
                                  UploadStorage uploadStorage) {
        this.designRepository = designRepository;
        this.designImageRepository = designImageRepository;
        this.uploadStorage = uploadStorage;
    }

    @Override
    @Transactional
    public DesignImage uploadImage(Long designId, MultipartFile file) {
        try {
            validateFile(file);

            Design design = designRepository.findById(designId)
                    .orElseThrow(() -> new RuntimeException("Design not found with id: " + designId));

            removeExistingImages(design);

            String fileName = buildFileName(designId, file.getOriginalFilename());
            try (var input = file.getInputStream()) {
                uploadStorage.save(input, fileName);
            }

            Path target = uploadStorage.resolve(fileName);
            DesignImage image = DesignImage.builder()
                    .design(design)
                    .imageName(fileName)
                    .imagePath(target.toString())
                    .displayOrder(0)
                    .isPrimary(true)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return designImageRepository.save(image);
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignImage> getImagesByDesignId(Long designId) {
        Design design = designRepository.findById(designId)
                .orElseThrow(() -> new RuntimeException("Design not found with id: " + designId));
        return designImageRepository.findByDesign(design);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        DesignImage image = designImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        deletePhysicalFile(image.getImagePath(), image.getImageName());
        designImageRepository.delete(image);
    }

    @Override
    @Transactional
    public DesignImage updateImage(Long imageId, MultipartFile file) {
        DesignImage image = designImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        return uploadImage(image.getDesign().getDesignId(), file);
    }

    private void removeExistingImages(Design design) {
        for (DesignImage existing : designImageRepository.findByDesign(design)) {
            deletePhysicalFile(existing.getImagePath(), existing.getImageName());
            designImageRepository.delete(existing);
        }
    }

    private void deletePhysicalFile(String filePath, String fileName) {
        try {
            if (filePath != null && !filePath.isBlank()) {
                uploadStorage.deleteIfExists(filePath);
            } else if (fileName != null && !fileName.isBlank()) {
                uploadStorage.deleteIfExists(fileName);
            }
        } catch (IOException ignored) {
            // Best-effort cleanup; DB row is still removed.
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new RuntimeException("Invalid image file name");
        }
        String ext = extension(original);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("Only JPG, PNG, WEBP, or GIF images are allowed");
        }
    }

    private String buildFileName(Long designId, String originalFilename) {
        return "design-" + designId + "-" + System.currentTimeMillis() + extension(originalFilename);
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "";
        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }
}
