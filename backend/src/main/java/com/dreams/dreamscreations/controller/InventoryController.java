package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.InventoryAdjustRequest;
import com.dreams.dreamscreations.dto.InventoryAdjustmentDTO;
import com.dreams.dreamscreations.dto.InventoryItemDTO;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;

    public InventoryController(InventoryService inventoryService,
                               CurrentUserService currentUserService) {
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemDTO>> getAll() {
        inventoryService.syncLowStockAlerts();
        return ResponseEntity.ok(inventoryService.getAllStock());
    }

    @GetMapping("/suit/{suitId}")
    public ResponseEntity<InventoryItemDTO> getBySuit(@PathVariable Long suitId) {
        return ResponseEntity.ok(inventoryService.getBySuitId(suitId));
    }

    @PostMapping("/suit/{suitId}/adjust")
    public ResponseEntity<InventoryItemDTO> adjustStock(@PathVariable Long suitId,
                                                        @RequestBody InventoryAdjustRequest request) {
        if (request.getNewQuantity() == null) {
            throw new RuntimeException("New quantity is required");
        }
        return ResponseEntity.ok(inventoryService.adjustStock(
                suitId,
                request.getNewQuantity(),
                request.getReason(),
                currentUserService.getCurrentUser()));
    }

    @GetMapping("/adjustments")
    public ResponseEntity<List<InventoryAdjustmentDTO>> getAdjustments() {
        return ResponseEntity.ok(inventoryService.getAdjustmentHistory());
    }
}
