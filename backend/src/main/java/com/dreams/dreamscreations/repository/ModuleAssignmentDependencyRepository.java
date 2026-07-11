package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.entity.ModuleAssignmentDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleAssignmentDependencyRepository extends JpaRepository<ModuleAssignmentDependency, Long> {
    // Find all dependencies that block a given assignment
    List<ModuleAssignmentDependency> findByAssignment(ModuleAssignment assignment);
}
