package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.DesigningWorkType;
import com.dreams.dreamscreations.repository.DesigningWorkTypeRepository;
import com.dreams.dreamscreations.service.DesigningWorkTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesigningWorkTypeServiceImpl implements DesigningWorkTypeService {

    private final DesigningWorkTypeRepository repo;

    public DesigningWorkTypeServiceImpl(DesigningWorkTypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public DesigningWorkType save(DesigningWorkType type) {
        if (type.getStatus() == null || type.getStatus().isBlank()) {
            type.setStatus("active");
        }
        return repo.save(type);
    }

    @Override
    public List<DesigningWorkType> getAllActive() {
        return repo.findByStatusOrderByTypeNameAsc("active");
    }

    @Override
    public DesigningWorkType getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Designing work type not found: " + id));
    }
}
