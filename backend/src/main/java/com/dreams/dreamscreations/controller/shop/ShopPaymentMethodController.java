package com.dreams.dreamscreations.controller.shop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/payment-methods")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopPaymentMethodController {

    private static final List<Map<String, String>> METHODS = List.of(
            Map.of("code", "cod", "label", "Cash on delivery (COD)",
                    "description", "Pay when your order is delivered"),
            Map.of("code", "bank_transfer", "label", "Bank transfer",
                    "description", "Transfer to our bank account and enter reference"),
            Map.of("code", "online_gateway", "label", "Online payment (coming soon)",
                    "description", "Card/wallet gateway stub — payment marked pending")
    );

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> list() {
        return ResponseEntity.ok(METHODS);
    }
}
