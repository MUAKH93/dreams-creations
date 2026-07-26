package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.DispatchRequest;
import com.dreams.dreamscreations.dto.ReturnRequest;
import com.dreams.dreamscreations.entity.ModuleAssignment;

import java.util.List;

public interface ModuleAssignmentService {
    ModuleAssignment dispatch(DispatchRequest request);
    ModuleAssignment dispatch(ModuleAssignment assignment);
    ModuleAssignment returnAssignment(Long assignmentId, ReturnRequest request);
    List<ModuleAssignment> getAll();
    ModuleAssignment getById(Long id);
    List<ModuleAssignment> getByBatchId(Long batchId);
    List<ModuleAssignment> getBySupervisorId(Long supervisorId);
    List<ModuleAssignment> getMineForCurrentSupervisor();
    List<ModuleAssignment> getOverdue();
}
