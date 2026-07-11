package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.repository.SupervisorRepository;
import com.dreams.dreamscreations.service.SupervisorService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SupervisorServiceImpl implements SupervisorService {

    private final SupervisorRepository repo;

    public SupervisorServiceImpl(SupervisorRepository repo) { this.repo = repo; }

    @Override public Supervisor save(Supervisor supervisor) { return repo.save(supervisor); }
    @Override public List<Supervisor> getAll() { return repo.findAll(); }

    @Override
    public Supervisor getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Supervisor not found: " + id));
    }

    @Override
    public Supervisor update(Long id, Supervisor updated) {
        Supervisor existing = getById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return repo.save(existing);
    }

    @Override public void delete(Long id) { repo.delete(getById(id)); }
}
