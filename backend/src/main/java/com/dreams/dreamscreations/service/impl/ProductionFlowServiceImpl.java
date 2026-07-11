package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.BatchFlowStatusDTO;
import com.dreams.dreamscreations.dto.ModuleFlowDTO;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.service.ProductionFlowService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionFlowServiceImpl implements ProductionFlowService {

    private final ProductionBatchRepository batchRepo;
    private final ModuleAssignmentRepository assignmentRepo;
    private final AlertRepository alertRepo;

    public ProductionFlowServiceImpl(ProductionBatchRepository batchRepo,
                                     ModuleAssignmentRepository assignmentRepo,
                                     AlertRepository alertRepo) {
        this.batchRepo = batchRepo;
        this.assignmentRepo = assignmentRepo;
        this.alertRepo = alertRepo;
    }

    @Override
    public BatchFlowStatusDTO getBatchFlowStatus(Long batchId) {

        ProductionBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));

        List<ModuleAssignment> allAssignments = assignmentRepo.findByBatch(batch);

        // Group by module
        Map<Long, List<ModuleAssignment>> byModule = allAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getModule().getModuleId()));

        List<ModuleFlowDTO> moduleFlows = new ArrayList<>();
        int totalInProgress = 0;
        int totalStuck      = 0;
        int totalLost       = 0;

        for (Map.Entry<Long, List<ModuleAssignment>> entry : byModule.entrySet()) {

            List<ModuleAssignment> moduleAssignments = entry.getValue();
            ProductionModule module = moduleAssignments.get(0).getModule();

            int sentToModule = moduleAssignments.stream()
                    .mapToInt(ModuleAssignment::getQuantitySent).sum();
            int returnedOk   = moduleAssignments.stream()
                    .mapToInt(ModuleAssignment::getQuantityReturnedOk).sum();
            int damaged      = moduleAssignments.stream()
                    .mapToInt(ModuleAssignment::getQuantityDamaged).sum();
            int missing      = moduleAssignments.stream()
                    .mapToInt(ModuleAssignment::getQuantityMissing).sum();

            // Pieces still INSIDE this module (not yet returned in any form)
            int pending = sentToModule - (returnedOk + damaged + missing);

            // Pieces forwarded to a higher stage
            // BUG FIX: cap at returnedOk — you can never forward more than you received back
            int rawForwarded = computeForwardedFromModule(module, allAssignments);
            int forwarded    = Math.min(rawForwarded, returnedOk);

            // Pieces returned OK but not yet forwarded anywhere
            int stuck = Math.max(0, returnedOk - forwarded);

            // Flow status
            String flowStatus;
            if (pending > 0 && returnedOk == 0) {
                flowStatus = "in_progress";
            } else if (pending > 0) {
                flowStatus = "in_progress";
            } else if (stuck > 0) {
                flowStatus = "stuck";
            } else if (returnedOk > 0 && pending == 0 && stuck == 0) {
                flowStatus = "completed";
            } else {
                flowStatus = "on_track";
            }

            totalInProgress += pending;
            totalStuck      += stuck;
            totalLost       += (damaged + missing);

            if (stuck > 0) {
                createStuckAlert(batch, module, stuck);
            }

            moduleFlows.add(ModuleFlowDTO.builder()
                    .batchId(batchId)
                    .moduleId(module.getModuleId())
                    .moduleName(module.getModuleName())
                    .stageName(module.getStage().getStageName())
                    .quantitySent(sentToModule)
                    .quantityReturnedOk(returnedOk)
                    .quantityDamaged(damaged)
                    .quantityMissing(missing)
                    .quantityPending(pending)
                    .quantityForwarded(forwarded)
                    .quantityStuck(stuck)
                    .flowStatus(flowStatus)
                    .build());
        }

        // Sort by stage order
        moduleFlows.sort(Comparator.comparingInt(f ->
            byModule.get(f.getModuleId()).get(0).getModule().getStage().getStageOrder()
        ));

        String overallStatus;
        if ("completed".equals(batch.getStatus())) {
            overallStatus = "completed";
        } else if (totalStuck > 0) {
            overallStatus = "has_bottleneck";
        } else {
            overallStatus = "on_track";
        }

        return BatchFlowStatusDTO.builder()
                .batchId(batchId)
                .batchNumber(batch.getBatchNumber())
                .totalPlanned(batch.getTotalSuitPlanned())
                .totalProduced(batch.getTotalSuitProduced())
                .totalInProgress(totalInProgress)
                .totalStuck(totalStuck)
                .totalLost(totalLost)
                .moduleFlows(moduleFlows)
                .overallStatus(overallStatus)
                .build();
    }

    @Override
    public ModuleFlowDTO getModuleFlow(Long batchId, Long moduleId) {
        return getBatchFlowStatus(batchId).getModuleFlows().stream()
                .filter(f -> f.getModuleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No flow data for batch " + batchId + " at module " + moduleId));
    }

    /**
     * How many pieces were forwarded ONWARD from this module?
     * = total sent to all modules whose stage order is HIGHER than this module's stage order.
     *
     * This is then capped at quantityReturnedOk in the caller —
     * you cannot forward more than you physically received back.
     */
    private int computeForwardedFromModule(ProductionModule fromModule,
                                            List<ModuleAssignment> allAssignments) {
        int fromStageOrder = fromModule.getStage().getStageOrder();
        return allAssignments.stream()
                .filter(a -> a.getModule().getStage().getStageOrder() > fromStageOrder)
                .mapToInt(ModuleAssignment::getQuantitySent)
                .sum();
    }

    private void createStuckAlert(ProductionBatch batch, ProductionModule module, int stuckCount) {
        boolean alreadyOpen = alertRepo.existsByRelatedEntityTypeAndRelatedEntityIdAndStatus(
                "MODULE_ASSIGNMENT", batch.getBatchId(), "open");
        if (!alreadyOpen) {
            alertRepo.save(Alert.builder()
                    .alertType("STUCK_PIECES")
                    .message(stuckCount + " pieces from batch '" + batch.getBatchNumber()
                            + "' were returned from '" + module.getModuleName()
                            + "' but have not been forwarded to the next module yet.")
                    .relatedEntityType("MODULE_ASSIGNMENT")
                    .relatedEntityId(batch.getBatchId())
                    .status("open")
                    .build());
        }
    }
}
