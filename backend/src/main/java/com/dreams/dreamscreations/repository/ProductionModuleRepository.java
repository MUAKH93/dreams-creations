package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ProductionModule;
import com.dreams.dreamscreations.entity.ProductionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionModuleRepository extends JpaRepository<ProductionModule, Long> {
    List<ProductionModule> findByStage(ProductionStage stage);
    List<ProductionModule> findByStatus(String status);
    List<ProductionModule> findByStageAndStatusOrderByModuleIdAsc(ProductionStage stage, String status);
}
