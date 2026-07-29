package com.dreams.dreamscreations.controller.shop;

import com.dreams.dreamscreations.dto.shop.ShopAnalyticsDTO;
import com.dreams.dreamscreations.service.shop.ShopAnalyticsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop/analytics")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopAnalyticsController {

    private final ShopAnalyticsService analyticsService;

    public ShopAnalyticsController(ShopAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<ShopAnalyticsDTO> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}
