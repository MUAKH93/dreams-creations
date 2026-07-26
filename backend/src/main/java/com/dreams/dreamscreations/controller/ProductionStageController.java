package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.ProductionStage;
import com.dreams.dreamscreations.service.ProductionStageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/production-stages")
public class ProductionStageController {

    private final ProductionStageService service;
    public ProductionStageController(ProductionStageService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProductionStage> create(@RequestBody ProductionStage stage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(stage));
    }

    @GetMapping
    public ResponseEntity<List<ProductionStage>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionStage> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionStage> update(@PathVariable Long id, @RequestBody ProductionStage stage) {
        return ResponseEntity.ok(service.update(id, stage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
