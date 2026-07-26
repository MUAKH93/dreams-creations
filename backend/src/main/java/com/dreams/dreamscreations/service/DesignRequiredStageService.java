package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.DesignRequiredStage;
import com.dreams.dreamscreations.entity.ProductionStage;

import java.util.List;

public interface DesignRequiredStageService {
    List<ProductionStage> getRequiredStagesForDesign(Long designId);
    ProductionStage getFirstStage(Long designId);
    ProductionStage getFinalStage(Long designId);
    ProductionStage getNextStage(Long designId, Long currentStageId);
    List<DesignRequiredStage> getStageConfig(Long designId);
    List<DesignRequiredStage> saveStageConfig(Long designId, List<Long> stageIds);
}
