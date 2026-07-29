package com.dreams.dreamscreations.controller.shop;

import com.dreams.dreamscreations.dto.shop.ShopCheckoutRequest;
import com.dreams.dreamscreations.dto.shop.ShopOrderDTO;
import com.dreams.dreamscreations.dto.shop.ShopOrderPaymentDTO;
import com.dreams.dreamscreations.dto.shop.ShopRecordPaymentRequest;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.shop.ShopOrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/orders")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopOrderController {

    private final ShopOrderService orderService;
    private final CurrentUserService currentUserService;

    public ShopOrderController(ShopOrderService orderService,
                               CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ShopOrderDTO> checkout(@RequestBody ShopCheckoutRequest request) {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(orderService.checkout(customerId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ShopOrderDTO>> getMyOrders() {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(orderService.getMyOrders(customerId));
    }

    @GetMapping("/my/{orderId}")
    public ResponseEntity<ShopOrderDTO> getMyOrder(@PathVariable Long orderId) {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(orderService.getMyOrder(customerId, orderId));
    }

    @PostMapping("/my/{orderId}/cancel")
    public ResponseEntity<ShopOrderDTO> cancelMyOrder(@PathVariable Long orderId) {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(orderService.cancelMyOrder(customerId, orderId));
    }

    @GetMapping
    public ResponseEntity<List<ShopOrderDTO>> getAllOrders(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getAllOrders(status));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ShopOrderDTO> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ShopOrderDTO> updateStatus(@PathVariable Long orderId,
                                                     @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, body.get("status")));
    }

    @PostMapping("/{orderId}/convert-to-quotation")
    public ResponseEntity<ShopOrderDTO> convertToQuotation(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.convertToQuotation(orderId));
    }

    @PostMapping("/{orderId}/convert-to-bill")
    public ResponseEntity<ShopOrderDTO> convertToBill(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.convertToBill(orderId));
    }

    @PostMapping("/{orderId}/payments")
    public ResponseEntity<ShopOrderDTO> recordPayment(@PathVariable Long orderId,
                                                      @RequestBody ShopRecordPaymentRequest request) {
        return ResponseEntity.ok(orderService.recordPayment(orderId, request));
    }

    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<ShopOrderPaymentDTO>> getPayments(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderPayments(orderId));
    }
}
