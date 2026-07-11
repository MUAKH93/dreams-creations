package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.FillingWorkType;
import com.dreams.dreamscreations.service.FillingWorkTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filling-work-types")
public class FillingWorkTypeController {

    private final FillingWorkTypeService service;

    public FillingWorkTypeController(FillingWorkTypeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FillingWorkType> create(@RequestBody FillingWorkType type) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(type));
    }

    @GetMapping
    public ResponseEntity<List<FillingWorkType>> getAll() {
        return ResponseEntity.ok(service.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FillingWorkType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
