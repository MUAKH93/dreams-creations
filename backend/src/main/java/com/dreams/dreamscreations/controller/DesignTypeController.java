package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.DesignType;
import com.dreams.dreamscreations.service.DesignTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/design-types")
public class DesignTypeController {

    private final DesignTypeService designTypeService;

    public DesignTypeController(DesignTypeService designTypeService) {
        this.designTypeService = designTypeService;
    }

    @PostMapping
    public ResponseEntity<DesignType> create(@RequestBody DesignType designType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(designTypeService.saveDesignType(designType));
    }

    @GetMapping
    public ResponseEntity<List<DesignType>> getAll() {
        return ResponseEntity.ok(designTypeService.getAllDesignTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(designTypeService.getDesignTypeById(id));
    }
}
