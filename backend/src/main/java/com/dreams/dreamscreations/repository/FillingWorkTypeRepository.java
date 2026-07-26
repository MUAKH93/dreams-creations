package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.FillingWorkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FillingWorkTypeRepository extends JpaRepository<FillingWorkType, Long> {
    List<FillingWorkType> findByStatusOrderByTypeNameAsc(String status);
}
