package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Size;
import com.dreams.dreamscreations.service.SizeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sizes")
public class SizeController {

    private final SizeService sizeService;

    public SizeController(SizeService sizeService) {
        this.sizeService = sizeService;
    }

    @PostMapping
    public ResponseEntity<Size> create(@RequestBody Size size) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sizeService.saveSize(size));
    }

    @GetMapping
    public ResponseEntity<List<Size>> getAll() {
        return ResponseEntity.ok(sizeService.getAllSizes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Size> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sizeService.getSizeById(id));
    }

    // GET /api/sizes/category/1 → all sizes for Ladies
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Size>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(sizeService.getSizesByCategoryId(categoryId));
    }
}
