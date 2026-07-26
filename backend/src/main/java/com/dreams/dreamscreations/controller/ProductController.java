package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.ProductStockDTO;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(product));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() { return ResponseEntity.ok(service.getAll()); }

    /** Products with live inventory quantity — use for billing */
    @GetMapping("/with-stock")
    public ResponseEntity<List<ProductStockDTO>> getAllWithStock() {
        return ResponseEntity.ok(service.getAllWithStock());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/suit/{suitId}")
    public ResponseEntity<Product> getBySuit(@PathVariable Long suitId) {
        return ResponseEntity.ok(service.getBySuitId(suitId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(service.update(id, product));
    }
}
