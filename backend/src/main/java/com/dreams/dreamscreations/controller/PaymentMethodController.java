package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.PaymentMethod;
import com.dreams.dreamscreations.service.PaymentMethodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService service;
    public PaymentMethodController(PaymentMethodService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<PaymentMethod> create(@RequestBody PaymentMethod pm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(pm));
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
