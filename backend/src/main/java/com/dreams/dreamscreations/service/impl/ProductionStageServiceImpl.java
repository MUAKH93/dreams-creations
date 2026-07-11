package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.ProductionStage;
import com.dreams.dreamscreations.repository.ProductionStageRepository;
import com.dreams.dreamscreations.service.ProductionStageService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductionStageServiceImpl implements ProductionStageService {

    private final ProductionStageRepository repo;

    public ProductionStageServiceImpl(ProductionStageRepository repo) {
        this.repo = repo;
    }

    @Override public ProductionStage save(ProductionStage stage) { return repo.save(stage); }

    @Override public List<ProductionStage> getAll() { return repo.findAllByOrderByStageOrderAsc(); }

    @Override
    public ProductionStage getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Production stage not found: " + id));
    }

    @Override
    public ProductionStage update(Long id, ProductionStage updated) {
        ProductionStage existing = getById(id);
        existing.setStageName(updated.getStageName());
        existing.setStageOrder(updated.getStageOrder());
        existing.setIsMandatory(updated.getIsMandatory());
        existing.setDescription(updated.getDescription());
        return repo.save(existing);
    }

    @Override public void delete(Long id) { repo.delete(getById(id)); }
}
