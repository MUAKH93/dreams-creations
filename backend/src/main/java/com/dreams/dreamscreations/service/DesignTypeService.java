package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.DesignType;
import java.util.List;

public interface DesignTypeService {
    DesignType saveDesignType(DesignType designType);
    List<DesignType> getAllDesignTypes();
    DesignType getDesignTypeById(Long id);
}
