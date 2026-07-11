package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Alert;
import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.repository.AlertRepository;
import com.dreams.dreamscreations.repository.ModuleAssignmentRepository;
import com.dreams.dreamscreations.repository.ProductionBatchRepository;
import com.dreams.dreamscreations.service.AlertService;
import com.dreams.dreamscreations.service.ProductionFlowService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepo;
    private final ModuleAssignmentRepository assignmentRepo;
    private final ProductionBatchRepository batchRepo;
    private final ProductionFlowService flowService;

    // @Lazy breaks the circular dependency:
    // AlertServiceImpl → ProductionFlowService → AlertRepository → AlertServiceImpl
    public AlertServiceImpl(AlertRepository alertRepo,
                            ModuleAssignmentRepository assignmentRepo,
                            ProductionBatchRepository batchRepo,
                            @Lazy ProductionFlowService flowService) {
        this.alertRepo = alertRepo;
        this.assignmentRepo = assignmentRepo;
        this.batchRepo = batchRepo;
        this.flowService = flowService;
    }

    @Override
    public List<Alert> getOpenAlerts() {
        return alertRepo.findByStatus("open");
    }

    @Override
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setStatus("resolved");
        alert.setResolvedDate(LocalDateTime.now());
        return alertRepo.save(alert);
    }

    /**
     * Runs every day at 8:00 AM.
     * Checks two things:
     *   1. Overdue dispatches (past due date, not returned)
     *   2. Stuck pieces (returned from a module but not forwarded onward)
     */
    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void checkAndCreateOverdueAlerts() {

        // --- Check 1: Overdue dispatches ---
        List<ModuleAssignment> overdueList =
                assignmentRepo.findOverdueAssignments(LocalDateTime.now());

        for (ModuleAssignment assignment : overdueList) {
            boolean alreadyOpen = alertRepo
                    .existsByRelatedEntityTypeAndRelatedEntityIdAndStatus(
                            "MODULE_ASSIGNMENT", assignment.getAssignmentId(), "open");

            if (!alreadyOpen) {
                Alert alert = Alert.builder()
                        .alertType("OVERDUE")
                        .message("Dispatch #" + assignment.getAssignmentId()
                                + " to '" + assignment.getModule().getModuleName()
                                + "' was due on " + assignment.getDueDate()
                                + " and has not been returned.")
                        .relatedEntityType("MODULE_ASSIGNMENT")
                        .relatedEntityId(assignment.getAssignmentId())
                        .status("open")
                        .build();
                alertRepo.save(alert);
                assignment.setStatus("overdue");
                assignmentRepo.save(assignment);
            }
        }

        // --- Check 2: Stuck pieces across all in-progress batches ---
        batchRepo.findByStatus("in_progress").forEach(batch -> {
            try {
                // getBatchFlowStatus internally creates STUCK_PIECES alerts
                // when it detects quantityStuck > 0 for any module
                flowService.getBatchFlowStatus(batch.getBatchId());
            } catch (Exception e) {
                // Log but don't crash the scheduler
                System.err.println("Flow check failed for batch "
                        + batch.getBatchId() + ": " + e.getMessage());
            }
        });
    }
}
