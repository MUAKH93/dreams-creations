package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ModuleAssignmentSkuLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleAssignmentSkuLineRepository extends JpaRepository<ModuleAssignmentSkuLine, Long> {
    List<ModuleAssignmentSkuLine> findByAssignment_AssignmentId(Long assignmentId);
}
