package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.service.SupervisorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/supervisors")
public class SupervisorController {

    private final SupervisorService service;
    public SupervisorController(SupervisorService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Supervisor> create(@RequestBody Supervisor supervisor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(supervisor));
    }

    @GetMapping
    public ResponseEntity<List<Supervisor>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Supervisor> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supervisor> update(@PathVariable Long id, @RequestBody Supervisor supervisor) {
        return ResponseEntity.ok(service.update(id, supervisor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
