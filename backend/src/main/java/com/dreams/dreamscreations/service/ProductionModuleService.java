package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.ProductionModule;
import java.util.List;

public interface ProductionModuleService {
    ProductionModule save(ProductionModule module);
    List<ProductionModule> getAll();
    ProductionModule getById(Long id);
    List<ProductionModule> getByStageId(Long stageId);
    ProductionModule update(Long id, ProductionModule module);
    void delete(Long id);
}
