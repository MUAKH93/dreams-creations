package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.SuitProductionTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuitProductionTrackingRepository extends JpaRepository<SuitProductionTracking, Long> {
    List<SuitProductionTracking> findByBatch(ProductionBatch batch);
    List<SuitProductionTracking> findByBatchAndStatus(ProductionBatch batch, String status);
}
