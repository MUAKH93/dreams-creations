package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.DesigningWorkType;

import java.util.List;

public interface DesigningWorkTypeService {
    DesigningWorkType save(DesigningWorkType type);
    List<DesigningWorkType> getAllActive();
    DesigningWorkType getById(Long id);
}
