package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.DesigningWorkType;
import com.dreams.dreamscreations.service.DesigningWorkTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designing-work-types")
public class DesigningWorkTypeController {

    private final DesigningWorkTypeService service;

    public DesigningWorkTypeController(DesigningWorkTypeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DesigningWorkType> create(@RequestBody DesigningWorkType type) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(type));
    }

    @GetMapping
    public ResponseEntity<List<DesigningWorkType>> getAll() {
        return ResponseEntity.ok(service.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesigningWorkType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
