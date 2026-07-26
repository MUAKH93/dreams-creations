package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.ProductionModule;
import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.entity.SupervisorModule;
import com.dreams.dreamscreations.repository.ProductionModuleRepository;
import com.dreams.dreamscreations.repository.SupervisorModuleRepository;
import com.dreams.dreamscreations.repository.SupervisorRepository;
import com.dreams.dreamscreations.service.SupervisorModuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SupervisorModuleServiceImpl implements SupervisorModuleService {

    private final SupervisorModuleRepository repo;
    private final SupervisorRepository supervisorRepo;
    private final ProductionModuleRepository moduleRepo;

    public SupervisorModuleServiceImpl(SupervisorModuleRepository repo,
                                       SupervisorRepository supervisorRepo,
                                       ProductionModuleRepository moduleRepo) {
        this.repo = repo;
        this.supervisorRepo = supervisorRepo;
        this.moduleRepo = moduleRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupervisorModule> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupervisorModule> getBySupervisorId(Long supervisorId) {
        Supervisor supervisor = supervisorRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + supervisorId));
        return repo.findBySupervisor(supervisor);
    }

    @Override
    @Transactional
    public SupervisorModule assign(Long supervisorId, Long moduleId) {
        Supervisor supervisor = supervisorRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + supervisorId));
        ProductionModule module = moduleRepo.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));

        boolean exists = repo.findBySupervisor(supervisor).stream()
                .anyMatch(sm -> sm.getModule().getModuleId().equals(moduleId));
        if (exists) {
            throw new RuntimeException("Supervisor is already assigned to module: " + module.getModuleName());
        }

        return repo.save(SupervisorModule.builder()
                .supervisor(supervisor)
                .module(module)
                .assignedDate(LocalDate.now())
                .status("active")
                .build());
    }

    @Override
    @Transactional
    public void unassign(Long supervisorModuleId) {
        SupervisorModule sm = repo.findById(supervisorModuleId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + supervisorModuleId));
        repo.delete(sm);
    }
}
