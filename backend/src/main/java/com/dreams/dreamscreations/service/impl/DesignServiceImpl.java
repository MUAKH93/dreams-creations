package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.config.UploadStorage;
import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import com.dreams.dreamscreations.repository.DesignImageRepository;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.repository.DesignRequiredStageRepository;
import com.dreams.dreamscreations.repository.SuitRepository;
import com.dreams.dreamscreations.service.ActivityLogService;
import com.dreams.dreamscreations.service.DesignService;
import com.dreams.dreamscreations.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class DesignServiceImpl implements DesignService {

    private final DesignRepository designRepository;
    private final DesignImageRepository designImageRepository;
    private final DesignRequiredStageRepository requiredStageRepository;
    private final SuitRepository suitRepository;
    private final UploadStorage uploadStorage;
    private final ActivityLogService activityLogService;
    private final CurrentUserService currentUserService;

    public DesignServiceImpl(DesignRepository designRepository,
                             DesignImageRepository designImageRepository,
                             DesignRequiredStageRepository requiredStageRepository,
                             SuitRepository suitRepository,
                             UploadStorage uploadStorage,
                             ActivityLogService activityLogService,
                             CurrentUserService currentUserService) {
        this.designRepository = designRepository;
        this.designImageRepository = designImageRepository;
        this.requiredStageRepository = requiredStageRepository;
        this.suitRepository = suitRepository;
        this.uploadStorage = uploadStorage;
        this.activityLogService = activityLogService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Design saveDesign(Design design) {
        if (design.getBasePrice() == null || design.getBasePrice().signum() <= 0) {
            throw new RuntimeException("Design price is required and must be greater than zero");
        }
        return designRepository.save(design);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Design> getAllDesigns() {
        return designRepository.findAllWithImages();
    }

    @Override
    @Transactional(readOnly = true)
    public Design getDesignById(Long id) {
        return designRepository.findByIdWithImages(id)
                .orElseThrow(() -> new RuntimeException("Design not found with id: " + id));
    }

    @Override
    public Design updateDesign(Long id, Design updated) {
        if (updated.getBasePrice() == null || updated.getBasePrice().signum() <= 0) {
            throw new RuntimeException("Design price is required and must be greater than zero");
        }
        Design existing = getDesignById(id);
        existing.setName(updated.getName());
        existing.setDesignCode(updated.getDesignCode());
        existing.setDescription(updated.getDescription());
        existing.setBasePrice(updated.getBasePrice());
        existing.setCategory(updated.getCategory());
        existing.setDesignType(updated.getDesignType());
        existing.setEmbroideryType(updated.getEmbroideryType());
        existing.setIsFeatured(updated.getIsFeatured());
        Design saved = designRepository.save(existing);
        activityLogService.log(currentUserService.getCurrentUser(), "DESIGN_UPDATED", "DESIGN", id,
                "Updated design " + saved.getDesignCode() + " — " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public void deleteDesign(Long id) {
        Design design = getDesignById(id);
        if (!suitRepository.findByDesign(design).isEmpty()) {
            throw new RuntimeException(
                    "Cannot delete design with existing suits/SKUs. Remove related production and stock first.");
        }

        for (DesignImage image : designImageRepository.findByDesign(design)) {
            deleteImageFile(image);
            designImageRepository.delete(image);
        }

        requiredStageRepository.deleteAll(
                requiredStageRepository.findByDesignOrderByStageOrderAsc(design));

        designRepository.delete(design);
    }

    private void deleteImageFile(DesignImage image) {
        try {
            if (image.getImagePath() != null) {
                uploadStorage.deleteIfExists(image.getImagePath());
            } else if (image.getImageName() != null) {
                uploadStorage.deleteIfExists(image.getImageName());
            }
        } catch (IOException ignored) {
            // Best-effort file cleanup.
        }
    }
}
