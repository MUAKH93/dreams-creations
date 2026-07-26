package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.service.BillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService service;
    public BillController(BillService service) { this.service = service; }

    /**
     * Create a bill with line items in one request.
     * Request body:
     * {
     *   "billNumber": "BILL-2026-001",
     *   "customer": { "customerId": 1 },
     *   "createdBy": { "userId": 1 },
     *   "discount": 500,
     *   "items": [
     *     { "product": { "productId": 1 }, "quantity": 10, "unitPrice": 2500 },
     *     { "product": { "productId": 2 }, "quantity": 5,  "unitPrice": 3000 }
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<Bill> create(@RequestBody Bill bill) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBill(bill));
    }

    @GetMapping
    public ResponseEntity<List<Bill>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/next-number")
    public ResponseEntity<Map<String, String>> getNextBillNumber() {
        return ResponseEntity.ok(Map.of("billNumber", service.generateNextBillNumber()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Bill>> getMyBills() {
        return ResponseEntity.ok(service.getMyBills());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Bill>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Bill>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // PATCH /api/bills/1/status  { "status": "paid" }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Bill> updateStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.updateStatus(id, body.get("status")));
    }
}
