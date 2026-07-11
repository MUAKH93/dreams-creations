package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Payment;
import com.dreams.dreamscreations.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }

    /**
     * Record a payment against a bill.
     * Request body:
     * {
     *   "bill": { "billId": 1 },
     *   "paymentMethod": { "paymentMethodId": 1 },
     *   "amount": 10000,
     *   "referenceNo": "TXN-12345",
     *   "notes": "First instalment"
     * }
     */
    @PostMapping
    public ResponseEntity<Payment> record(@RequestBody Payment payment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.recordPayment(payment));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<List<Payment>> getByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(service.getByBillId(billId));
    }
}
