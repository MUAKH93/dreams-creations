package com.dreams.dreamscreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full production flow summary for one batch.
 * Returned by GET /api/production-flow/{batchId}
 *
 * This is what powers the manager's production dashboard —
 * a single API call gives the complete picture of where
 * every piece in the batch is at any point in time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchFlowStatusDTO {

    private Long batchId;
    private String batchNumber;
    private int totalPlanned;
    private int totalProduced;       // finished pieces (returned from final stage)
    private int totalInProgress;     // inside any module right now
    private int totalStuck;          // returned from a module but not forwarded
    private int totalLost;           // damaged + missing across all modules

    private List<ModuleFlowDTO> moduleFlows;  // one entry per module that touched this batch

    private String overallStatus;    // "on_track", "has_bottleneck", "completed"
}
