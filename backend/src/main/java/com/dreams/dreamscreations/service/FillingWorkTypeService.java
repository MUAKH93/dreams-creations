package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.FillingWorkType;

import java.util.List;

public interface FillingWorkTypeService {
    FillingWorkType save(FillingWorkType type);
    List<FillingWorkType> getAllActive();
    FillingWorkType getById(Long id);
}
