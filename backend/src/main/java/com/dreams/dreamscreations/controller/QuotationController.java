package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Quotation;
import com.dreams.dreamscreations.service.QuotationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService service;

    public QuotationController(QuotationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Quotation> create(@RequestBody Quotation quotation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(quotation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quotation> update(@PathVariable Long id, @RequestBody Quotation quotation) {
        return ResponseEntity.ok(service.update(id, quotation));
    }

    @GetMapping
    public ResponseEntity<List<Quotation>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Quotation>> getMyQuotations() {
        return ResponseEntity.ok(service.getMyQuotations());
    }

    @GetMapping("/next-number")
    public ResponseEntity<Map<String, String>> getNextNumber() {
        return ResponseEntity.ok(Map.of("quotationNumber", service.generateNextQuotationNumber()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Quotation> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Quotation> updateStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.updateStatus(id, body.get("status")));
    }

    @PostMapping("/{id}/convert-to-bill")
    public ResponseEntity<Quotation> convertToBill(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToBill(id));
    }
}
