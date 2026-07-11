package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.entity.SupervisorModule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupervisorModuleRepository extends JpaRepository<SupervisorModule, Long> {
    List<SupervisorModule> findBySupervisor(Supervisor supervisor);
    List<SupervisorModule> findByStatus(String status);
}
