package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.DispatchRequest;
import com.dreams.dreamscreations.dto.ReturnRequest;
import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.service.ModuleAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/module-assignments")
public class ModuleAssignmentController {

    private final ModuleAssignmentService service;

    public ModuleAssignmentController(ModuleAssignmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ModuleAssignment> dispatch(@RequestBody DispatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dispatch(request));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ModuleAssignment> returnAssignment(
            @PathVariable Long id,
            @RequestBody ReturnRequest body) {
        return ResponseEntity.ok(service.returnAssignment(id, body));
    }

    @GetMapping
    public ResponseEntity<List<ModuleAssignment>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ModuleAssignment>> getMine() {
        return ResponseEntity.ok(service.getMineForCurrentSupervisor());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ModuleAssignment>> getOverdue() {
        return ResponseEntity.ok(service.getOverdue());
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<ModuleAssignment>> getByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(service.getByBatchId(batchId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleAssignment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
