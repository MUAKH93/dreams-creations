package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ProductionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionStageRepository extends JpaRepository<ProductionStage, Long> {
    List<ProductionStage> findAllByOrderByStageOrderAsc();
    java.util.Optional<ProductionStage> findByStageName(String stageName);
}
