package com.dreams.dreamscreations.controller.shop;

import com.dreams.dreamscreations.dto.shop.ShopCartDTO;
import com.dreams.dreamscreations.dto.shop.ShopCartItemRequest;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.shop.ShopCartService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/cart")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopCartController {

    private final ShopCartService cartService;
    private final CurrentUserService currentUserService;

    public ShopCartController(ShopCartService cartService,
                              CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ShopCartDTO> getCart() {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    public ResponseEntity<ShopCartDTO> addItem(@RequestBody ShopCartItemRequest request) {
        Long customerId = currentUserService.requireCustomerId();
        int qty = request.getQuantity() != null ? request.getQuantity() : 1;
        return ResponseEntity.ok(cartService.addItem(customerId, request.getProductId(), qty));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ShopCartDTO> updateItem(@PathVariable Long itemId,
                                                  @RequestBody Map<String, Integer> body) {
        Long customerId = currentUserService.requireCustomerId();
        Integer quantity = body.get("quantity");
        if (quantity == null) {
            throw new RuntimeException("quantity is required");
        }
        return ResponseEntity.ok(cartService.updateItemQuantity(customerId, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ShopCartDTO> removeItem(@PathVariable Long itemId) {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(cartService.removeItem(customerId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<ShopCartDTO> clearCart() {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(cartService.clearCart(customerId));
    }

    @PostMapping("/merge-guest")
    public ResponseEntity<ShopCartDTO> mergeGuest(@RequestBody List<ShopCartItemRequest> items) {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(cartService.mergeGuestItems(customerId, items));
    }
}
