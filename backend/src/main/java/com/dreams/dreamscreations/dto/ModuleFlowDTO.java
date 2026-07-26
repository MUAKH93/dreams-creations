package com.dreams.dreamscreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Quantity flow snapshot for one module within one batch.
 *
 * Example scenario:
 *   Cutting received 200 pieces
 *   Cutting returned 195 OK, 3 damaged, 2 missing
 *   Of those 195 OK, only 150 were forwarded to Stitching
 *   → quantityStuck = 45 (sitting idle, should trigger an alert)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleFlowDTO {

    private Long batchId;
    private Long moduleId;
    private String moduleName;
    private String stageName;

    private int quantitySent;         // total sent INTO this module
    private int quantityReturnedOk;   // came back good
    private int quantityDamaged;      // came back damaged
    private int quantityMissing;      // never came back
    private int quantityPending;      // still inside module (sent - returned)
    private int quantityForwarded;    // forwarded onward to the next module
    private int quantityStuck;        // returned OK but NOT forwarded yet → alert candidate

    private String flowStatus;        // "on_track", "stuck", "in_progress", "completed"
}
