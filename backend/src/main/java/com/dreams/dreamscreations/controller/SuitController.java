package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.service.SuitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suits")
public class SuitController {

    private final SuitService service;
    public SuitController(SuitService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Suit> create(@RequestBody Suit suit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(suit));
    }

    @GetMapping
    public ResponseEntity<List<Suit>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Suit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/design/{designId}")
    public ResponseEntity<List<Suit>> getByDesign(@PathVariable Long designId) {
        return ResponseEntity.ok(service.getByDesignId(designId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Suit> update(@PathVariable Long id, @RequestBody Suit suit) {
        return ResponseEntity.ok(service.update(id, suit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
