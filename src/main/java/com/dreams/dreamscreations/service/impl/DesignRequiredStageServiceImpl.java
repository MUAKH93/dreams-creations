package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignRequiredStage;
import com.dreams.dreamscreations.entity.ProductionStage;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.repository.DesignRequiredStageRepository;
import com.dreams.dreamscreations.repository.ProductionStageRepository;
import com.dreams.dreamscreations.service.DesignRequiredStageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DesignRequiredStageServiceImpl implements DesignRequiredStageService {

    private static final List<String> DEFAULT_STAGE_NAMES =
            List.of("Designing", "Cutting & Stitching");

    /** Legacy DBs may still use "Cutting" only as the combined stage */
    private static final List<String> FINAL_STAGE_ALIASES =
            List.of("Cutting & Stitching", "Cutting", "Stitching");

    private final DesignRepository designRepo;
    private final DesignRequiredStageRepository stageConfigRepo;
    private final ProductionStageRepository productionStageRepo;

    public DesignRequiredStageServiceImpl(DesignRepository designRepo,
                                          DesignRequiredStageRepository stageConfigRepo,
                                          ProductionStageRepository productionStageRepo) {
        this.designRepo = designRepo;
        this.stageConfigRepo = stageConfigRepo;
        this.productionStageRepo = productionStageRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionStage> getRequiredStagesForDesign(Long designId) {
        Design design = designRepo.findById(designId)
                .orElseThrow(() -> new RuntimeException("Design not found: " + designId));

        List<DesignRequiredStage> config = stageConfigRepo.findByDesignOrderByStageOrderAsc(design);
        if (!config.isEmpty()) {
            return config.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsRequired()))
                    .map(DesignRequiredStage::getStage)
                    .toList();
        }

        return resolveStagesByName(DEFAULT_STAGE_NAMES);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionStage getFirstStage(Long designId) {
        List<ProductionStage> stages = getRequiredStagesForDesign(designId);
        if (stages.isEmpty()) {
            throw new RuntimeException("No production stages configured");
        }
        return stages.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionStage getFinalStage(Long designId) {
        List<ProductionStage> stages = getRequiredStagesForDesign(designId);
        if (stages.isEmpty()) {
            throw new RuntimeException("No production stages configured");
        }
        return stages.get(stages.size() - 1);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionStage getNextStage(Long designId, Long currentStageId) {
        List<ProductionStage> stages = getRequiredStagesForDesign(designId);
        for (int i = 0; i < stages.size() - 1; i++) {
            if (stages.get(i).getStageId().equals(currentStageId)) {
                return stages.get(i + 1);
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignRequiredStage> getStageConfig(Long designId) {
        Design design = designRepo.findById(designId)
                .orElseThrow(() -> new RuntimeException("Design not found: " + designId));
        return stageConfigRepo.findByDesignOrderByStageOrderAsc(design);
    }

    @Override
    @Transactional
    public List<DesignRequiredStage> saveStageConfig(Long designId, List<Long> stageIds) {
        Design design = designRepo.findById(designId)
                .orElseThrow(() -> new RuntimeException("Design not found: " + designId));

        List<DesignRequiredStage> existing = stageConfigRepo.findByDesignOrderByStageOrderAsc(design);
        stageConfigRepo.deleteAll(existing);

        List<DesignRequiredStage> saved = new ArrayList<>();
        int order = 1;
        for (Long stageId : stageIds) {
            ProductionStage stage = productionStageRepo.findById(stageId)
                    .orElseThrow(() -> new RuntimeException("Stage not found: " + stageId));
            saved.add(stageConfigRepo.save(DesignRequiredStage.builder()
                    .design(design)
                    .stage(stage)
                    .stageOrder(order++)
                    .isRequired(true)
                    .build()));
        }
        return saved;
    }

    private List<ProductionStage> resolveStagesByName(List<String> names) {
        List<ProductionStage> result = new ArrayList<>();
        for (String name : names) {
            resolveStageByNameOrAlias(name).ifPresent(result::add);
        }
        if (result.isEmpty()) {
            return productionStageRepo.findAllByOrderByStageOrderAsc();
        }
        return result;
    }

    private java.util.Optional<ProductionStage> resolveStageByNameOrAlias(String name) {
        return productionStageRepo.findByStageName(name)
                .or(() -> {
                    if ("Cutting & Stitching".equals(name)) {
                        for (String alias : FINAL_STAGE_ALIASES) {
                            var found = productionStageRepo.findByStageName(alias);
                            if (found.isPresent()) return found;
                        }
                    }
                    return java.util.Optional.empty();
                });
    }
}
