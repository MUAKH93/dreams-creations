package com.dreams.dreamscreations.controller.integration;

import com.dreams.dreamscreations.dto.integration.ShopOrderSyncRequest;
import com.dreams.dreamscreations.dto.integration.ShopOrderSyncResponse;
import com.dreams.dreamscreations.service.integration.ShopFinanceIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shop → ERP integration: creates bills and payments so Finance auto-posting runs.
 * Secured by {@link com.dreams.dreamscreations.security.ShopIntegrationAuthFilter}.
 */
@RestController
@RequestMapping("/api/integration/shop")
public class ShopIntegrationController {

    private final ShopFinanceIntegrationService integrationService;

    public ShopIntegrationController(ShopFinanceIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping("/orders/sync")
    public ResponseEntity<ShopOrderSyncResponse> syncOrder(@RequestBody ShopOrderSyncRequest request) {
        return ResponseEntity.ok(integrationService.syncShopOrder(request));
    }
}
