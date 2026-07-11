package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.FillingWorkType;
import com.dreams.dreamscreations.repository.FillingWorkTypeRepository;
import com.dreams.dreamscreations.service.FillingWorkTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FillingWorkTypeServiceImpl implements FillingWorkTypeService {

    private final FillingWorkTypeRepository repo;

    public FillingWorkTypeServiceImpl(FillingWorkTypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public FillingWorkType save(FillingWorkType type) {
        if (type.getStatus() == null || type.getStatus().isBlank()) {
            type.setStatus("active");
        }
        return repo.save(type);
    }

    @Override
    public List<FillingWorkType> getAllActive() {
        return repo.findByStatusOrderByTypeNameAsc("active");
    }

    @Override
    public FillingWorkType getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Filling work type not found: " + id));
    }
}
