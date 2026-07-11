package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.ProductionModule;
import com.dreams.dreamscreations.entity.ProductionStage;
import com.dreams.dreamscreations.repository.ProductionModuleRepository;
import com.dreams.dreamscreations.repository.ProductionStageRepository;
import com.dreams.dreamscreations.service.ProductionModuleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductionModuleServiceImpl implements ProductionModuleService {

    private final ProductionModuleRepository moduleRepo;
    private final ProductionStageRepository stageRepo;

    public ProductionModuleServiceImpl(ProductionModuleRepository moduleRepo,
                                       ProductionStageRepository stageRepo) {
        this.moduleRepo = moduleRepo;
        this.stageRepo = stageRepo;
    }

    @Override public ProductionModule save(ProductionModule module) { return moduleRepo.save(module); }
    @Override public List<ProductionModule> getAll() { return moduleRepo.findAll(); }

    @Override
    public ProductionModule getById(Long id) {
        return moduleRepo.findById(id).orElseThrow(() -> new RuntimeException("Module not found: " + id));
    }

    @Override
    public List<ProductionModule> getByStageId(Long stageId) {
        ProductionStage stage = stageRepo.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage not found: " + stageId));
        return moduleRepo.findByStage(stage);
    }

    @Override
    public ProductionModule update(Long id, ProductionModule updated) {
        ProductionModule existing = getById(id);
        existing.setModuleName(updated.getModuleName());
        existing.setStage(updated.getStage());
        existing.setStatus(updated.getStatus());
        existing.setDescription(updated.getDescription());
        return moduleRepo.save(existing);
    }

    @Override public void delete(Long id) { moduleRepo.delete(getById(id)); }
}
