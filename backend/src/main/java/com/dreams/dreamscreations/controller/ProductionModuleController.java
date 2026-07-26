package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.ProductionModule;
import com.dreams.dreamscreations.service.ProductionModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/production-modules")
public class ProductionModuleController {

    private final ProductionModuleService service;
    public ProductionModuleController(ProductionModuleService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProductionModule> create(@RequestBody ProductionModule module) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(module));
    }

    @GetMapping
    public ResponseEntity<List<ProductionModule>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionModule> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/stage/{stageId}")
    public ResponseEntity<List<ProductionModule>> getByStage(@PathVariable Long stageId) {
        return ResponseEntity.ok(service.getByStageId(stageId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionModule> update(@PathVariable Long id, @RequestBody ProductionModule module) {
        return ResponseEntity.ok(service.update(id, module));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
