package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Supervisor;
import java.util.List;

public interface SupervisorService {
    Supervisor save(Supervisor supervisor);
    List<Supervisor> getAll();
    Supervisor getById(Long id);
    Supervisor update(Long id, Supervisor supervisor);
    void delete(Long id);
}
