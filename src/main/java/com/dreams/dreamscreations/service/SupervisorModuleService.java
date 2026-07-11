package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.SupervisorModule;

import java.util.List;

public interface SupervisorModuleService {
    List<SupervisorModule> getAll();
    List<SupervisorModule> getBySupervisorId(Long supervisorId);
    SupervisorModule assign(Long supervisorId, Long moduleId);
    void unassign(Long supervisorModuleId);
}
