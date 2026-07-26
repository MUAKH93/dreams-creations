package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.ProductionSettingsDTO;
import com.dreams.dreamscreations.dto.UpdateProductionSettingsRequest;
import com.dreams.dreamscreations.entity.Supervisor;

public interface ProductionSettingsService {
    ProductionSettingsDTO getSettings();
    ProductionSettingsDTO updateSettings(UpdateProductionSettingsRequest request);
    Supervisor requirePackingSupervisor();
}
