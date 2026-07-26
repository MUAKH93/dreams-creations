package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.DesigningWorkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesigningWorkTypeRepository extends JpaRepository<DesigningWorkType, Long> {
    List<DesigningWorkType> findByStatusOrderByTypeNameAsc(String status);
}
