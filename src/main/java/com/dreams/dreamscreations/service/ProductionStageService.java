package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.ProductionStage;
import java.util.List;

public interface ProductionStageService {
    ProductionStage save(ProductionStage stage);
    List<ProductionStage> getAll();
    ProductionStage getById(Long id);
    ProductionStage update(Long id, ProductionStage stage);
    void delete(Long id);
}
