package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.BatchUpdateRequest;
import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.User;
import java.util.List;

public interface ProductionBatchService {
    ProductionBatch save(ProductionBatch batch);
    List<ProductionBatch> getAll();
    ProductionBatch getById(Long id);
    List<ProductionBatch> getBySuitId(Long suitId);
    List<ProductionBatch> getByStatus(String status);
    ProductionBatch update(Long id, ProductionBatch batch);
    ProductionBatch updateBatch(Long id, BatchUpdateRequest request, User user);
    ProductionBatch cancelBatch(Long id, User user);
    void delete(Long id);
}
