package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.ProductionSettingsDTO;
import com.dreams.dreamscreations.dto.UpdateProductionSettingsRequest;
import com.dreams.dreamscreations.service.ProductionSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/production-settings")
public class ProductionSettingsController {

    private final ProductionSettingsService productionSettingsService;

    public ProductionSettingsController(ProductionSettingsService productionSettingsService) {
        this.productionSettingsService = productionSettingsService;
    }

    @GetMapping
    public ResponseEntity<ProductionSettingsDTO> getSettings() {
        return ResponseEntity.ok(productionSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<ProductionSettingsDTO> updateSettings(
            @RequestBody UpdateProductionSettingsRequest request) {
        return ResponseEntity.ok(productionSettingsService.updateSettings(request));
    }
}
