package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.ProductionStartRequest;
import com.dreams.dreamscreations.dto.ProductionStartResponse;
import com.dreams.dreamscreations.service.ProductionWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production")
public class ProductionWorkflowController {

    private final ProductionWorkflowService workflowService;

    public ProductionWorkflowController(ProductionWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Create a production order from design specification (design phase).
     * POST /api/production/start-order
     */
    @PostMapping("/start-order")
    public ResponseEntity<ProductionStartResponse> startOrder(
            @RequestBody ProductionStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.startProductionOrder(request));
    }
}
