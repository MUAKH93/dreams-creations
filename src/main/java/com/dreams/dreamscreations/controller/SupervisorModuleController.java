package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.SupervisorModule;
import com.dreams.dreamscreations.service.SupervisorModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervisor-modules")
public class SupervisorModuleController {

    private final SupervisorModuleService service;

    public SupervisorModuleController(SupervisorModuleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SupervisorModule>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<List<SupervisorModule>> getBySupervisor(@PathVariable Long supervisorId) {
        return ResponseEntity.ok(service.getBySupervisorId(supervisorId));
    }

    @PostMapping
    public ResponseEntity<SupervisorModule> assign(@RequestBody Map<String, Object> body) {
        Long supervisorId = Long.valueOf(body.get("supervisorId").toString());
        Long moduleId = Long.valueOf(body.get("moduleId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.assign(supervisorId, moduleId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unassign(@PathVariable Long id) {
        service.unassign(id);
        return ResponseEntity.noContent().build();
    }
}
