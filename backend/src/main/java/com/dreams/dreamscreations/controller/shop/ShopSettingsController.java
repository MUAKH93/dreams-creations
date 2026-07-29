package com.dreams.dreamscreations.controller.shop;

import com.dreams.dreamscreations.dto.shop.ShopSettingsDTO;
import com.dreams.dreamscreations.service.shop.ShopSettingsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop/settings")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopSettingsController {

    private final ShopSettingsService settingsService;

    public ShopSettingsController(ShopSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<ShopSettingsDTO> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<ShopSettingsDTO> updateSettings(@RequestBody ShopSettingsDTO request) {
        return ResponseEntity.ok(settingsService.updateSettings(request));
    }
}
