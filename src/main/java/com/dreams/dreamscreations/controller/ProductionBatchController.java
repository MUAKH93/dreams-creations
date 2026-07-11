package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.service.ProductionBatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/production-batches")
public class ProductionBatchController {

    private final ProductionBatchService service;
    public ProductionBatchController(ProductionBatchService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProductionBatch> create(@RequestBody ProductionBatch batch) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(batch));
    }

    @GetMapping
    public ResponseEntity<List<ProductionBatch>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionBatch> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/suit/{suitId}")
    public ResponseEntity<List<ProductionBatch>> getBySuit(@PathVariable Long suitId) {
        return ResponseEntity.ok(service.getBySuitId(suitId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductionBatch>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionBatch> update(@PathVariable Long id, @RequestBody ProductionBatch batch) {
        return ResponseEntity.ok(service.update(id, batch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
