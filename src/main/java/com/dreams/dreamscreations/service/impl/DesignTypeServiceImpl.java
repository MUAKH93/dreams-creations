package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.DesignType;
import com.dreams.dreamscreations.repository.DesignTypeRepository;
import com.dreams.dreamscreations.service.DesignTypeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DesignTypeServiceImpl implements DesignTypeService {

    private final DesignTypeRepository designTypeRepository;

    public DesignTypeServiceImpl(DesignTypeRepository designTypeRepository) {
        this.designTypeRepository = designTypeRepository;
    }

    @Override
    public DesignType saveDesignType(DesignType designType) {
        return designTypeRepository.save(designType);
    }

    @Override
    public List<DesignType> getAllDesignTypes() {
        return designTypeRepository.findAll();
    }

    @Override
    public DesignType getDesignTypeById(Long id) {
        return designTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DesignType not found: " + id));
    }
}
