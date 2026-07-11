package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.BatchFlowStatusDTO;
import com.dreams.dreamscreations.dto.ModuleFlowDTO;
import com.dreams.dreamscreations.service.ProductionFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Production flow tracking endpoints.
 *
 * GET /api/production-flow/{batchId}
 *   → Full quantity flow for a batch across all modules
 *   → Used by the manager dashboard to see where every piece is
 *
 * GET /api/production-flow/{batchId}/module/{moduleId}
 *   → Flow for one specific module in a batch
 *   → Used on the module detail screen
 *
 * This replaces the old "dependency blocking" approach with a
 * quantity-flow model:
 *   - Pieces flow freely between modules
 *   - System tracks what's been received vs forwarded
 *   - Stuck pieces (received but not forwarded) trigger alerts
 *   - Managers see the bottleneck and act — system doesn't block
 */
@RestController
@RequestMapping("/api/production-flow")
public class ProductionFlowController {

    private final ProductionFlowService flowService;

    public ProductionFlowController(ProductionFlowService flowService) {
        this.flowService = flowService;
    }

    /**
     * Full batch flow status — the main dashboard endpoint.
     *
     * Example response:
     * {
     *   "batchId": 1,
     *   "batchNumber": "BATCH-2026-001",
     *   "totalPlanned": 500,
     *   "totalProduced": 390,
     *   "totalInProgress": 50,
     *   "totalStuck": 45,
     *   "totalLost": 10,
     *   "overallStatus": "has_bottleneck",
     *   "moduleFlows": [
     *     {
     *       "moduleName": "Cutting Table 1",
     *       "stageName": "Cutting",
     *       "quantitySent": 400,
     *       "quantityReturnedOk": 390,
     *       "quantityDamaged": 5,
     *       "quantityMissing": 5,
     *       "quantityPending": 0,
     *       "quantityForwarded": 345,
     *       "quantityStuck": 45,
     *       "flowStatus": "stuck"
     *     },
     *     {
     *       "moduleName": "Stitching Line A",
     *       "stageName": "Stitching",
     *       "quantitySent": 345,
     *       "quantityReturnedOk": 0,
     *       "quantityPending": 345,
     *       "quantityForwarded": 0,
     *       "quantityStuck": 0,
     *       "flowStatus": "in_progress"
     *     }
     *   ]
     * }
     */
    @GetMapping("/{batchId}")
    public ResponseEntity<BatchFlowStatusDTO> getBatchFlow(@PathVariable Long batchId) {
        return ResponseEntity.ok(flowService.getBatchFlowStatus(batchId));
    }

    /**
     * Flow for one module in a batch — used on drill-down screens.
     */
    @GetMapping("/{batchId}/module/{moduleId}")
    public ResponseEntity<ModuleFlowDTO> getModuleFlow(@PathVariable Long batchId,
                                                        @PathVariable Long moduleId) {
        return ResponseEntity.ok(flowService.getModuleFlow(batchId, moduleId));
    }
}
