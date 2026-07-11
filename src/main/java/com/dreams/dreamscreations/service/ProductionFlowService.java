package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.BatchFlowStatusDTO;
import com.dreams.dreamscreations.dto.ModuleFlowDTO;

public interface ProductionFlowService {

    /**
     * Returns the full quantity-flow status of a batch across all modules.
     * This powers the main production tracking dashboard.
     *
     * For each module that received pieces from this batch, it shows:
     *   - quantitySent       → how many went in
     *   - quantityReturnedOk → how many came back good
     *   - quantityPending    → still inside this module (sent but not returned)
     *   - quantityForwarded  → how many were sent onward to the next module
     *   - quantityStuck      → returned here but not yet forwarded → triggers alert
     */
    BatchFlowStatusDTO getBatchFlowStatus(Long batchId);

    /**
     * Computes how many pieces are "stuck" at a module —
     * returned from that module but not dispatched to any subsequent module.
     * If this exceeds zero for more than a configured time, an alert is created.
     */
    ModuleFlowDTO getModuleFlow(Long batchId, Long moduleId);
}
