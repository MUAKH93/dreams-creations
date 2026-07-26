package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignRequiredStage;
import com.dreams.dreamscreations.entity.ProductionStage;
import com.dreams.dreamscreations.service.DesignRequiredStageService;
import com.dreams.dreamscreations.service.DesignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Design CRUD operations.
 *
 * Why ResponseEntity instead of returning the object directly?
 * Returning `Design` directly always gives HTTP 200. ResponseEntity
 * lets you control the status code precisely:
 *   201 Created  → when a new resource is successfully created
 *   200 OK       → for reads and updates
 *   204 No Content → for deletes (success but nothing to return)
 *   404 Not Found  → thrown by the service when an ID doesn't exist
 *
 * Why constructor injection instead of @Autowired on the field?
 * Same reason as the service: final + constructor = immutable,
 * testable, and Spring wires it automatically without the annotation.
 */
@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignService designService;
    private final DesignRequiredStageService stageService;

    public DesignController(DesignService designService,
                            DesignRequiredStageService stageService) {
        this.designService = designService;
        this.stageService = stageService;
    }

    // POST /api/designs
    @PostMapping
    public ResponseEntity<Design> createDesign(@RequestBody Design design) {
        Design saved = designService.saveDesign(design);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/designs
    @GetMapping
    public ResponseEntity<List<Design>> getAllDesigns() {
        return ResponseEntity.ok(designService.getAllDesigns());
    }

    // GET /api/designs/5
    @GetMapping("/{id}")
    public ResponseEntity<Design> getDesignById(@PathVariable Long id) {
        return ResponseEntity.ok(designService.getDesignById(id));
    }

    // PUT /api/designs/5
    @PutMapping("/{id}")
    public ResponseEntity<Design> updateDesign(@PathVariable Long id,
                                               @RequestBody Design design) {
        return ResponseEntity.ok(designService.updateDesign(id, design));
    }

    // DELETE /api/designs/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesign(@PathVariable Long id) {
        designService.deleteDesign(id);
        return ResponseEntity.noContent().build();   // 204
    }

    /** GET /api/designs/{id}/required-stages — ordered stage path for this design */
    @GetMapping("/{id}/required-stages")
    public ResponseEntity<List<ProductionStage>> getRequiredStages(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.getRequiredStagesForDesign(id));
    }

    /** PUT /api/designs/{id}/required-stages — body: [stageId, stageId, ...] */
    @PutMapping("/{id}/required-stages")
    public ResponseEntity<List<DesignRequiredStage>> saveRequiredStages(
            @PathVariable Long id, @RequestBody List<Long> stageIds) {
        return ResponseEntity.ok(stageService.saveStageConfig(id, stageIds));
    }
}
