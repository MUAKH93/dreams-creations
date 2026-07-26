package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignRequiredStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignRequiredStageRepository extends JpaRepository<DesignRequiredStage, Long> {
    List<DesignRequiredStage> findByDesignOrderByStageOrderAsc(Design design);
    boolean existsByDesign(Design design);
}
