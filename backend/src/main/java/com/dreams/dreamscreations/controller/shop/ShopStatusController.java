package com.dreams.dreamscreations.controller.shop;

import com.dreams.dreamscreations.dto.shop.ShopModuleStatusDTO;
import com.dreams.dreamscreations.service.shop.ShopModuleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopStatusController {

    private final ShopModuleService shopModuleService;

    public ShopStatusController(ShopModuleService shopModuleService) {
        this.shopModuleService = shopModuleService;
    }

    @GetMapping("/status")
    public ResponseEntity<ShopModuleStatusDTO> status() {
        return ResponseEntity.ok(shopModuleService.getStatus());
    }
}
