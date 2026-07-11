package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.ProductionBatch;
import java.util.List;

public interface ProductionBatchService {
    ProductionBatch save(ProductionBatch batch);
    List<ProductionBatch> getAll();
    ProductionBatch getById(Long id);
    List<ProductionBatch> getBySuitId(Long suitId);
    List<ProductionBatch> getByStatus(String status);
    ProductionBatch update(Long id, ProductionBatch batch);
    void delete(Long id);
}
