package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Alert;
import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.repository.AlertRepository;
import com.dreams.dreamscreations.repository.BillRepository;
import com.dreams.dreamscreations.repository.ModuleAssignmentRepository;
import com.dreams.dreamscreations.repository.ProductionBatchRepository;
import com.dreams.dreamscreations.service.AlertService;
import com.dreams.dreamscreations.service.ProductionFlowService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    public static final int PAYMENT_OVERDUE_DAYS = 30;

    private final AlertRepository alertRepo;
    private final ModuleAssignmentRepository assignmentRepo;
    private final ProductionBatchRepository batchRepo;
    private final BillRepository billRepo;
    private final ProductionFlowService flowService;

    public AlertServiceImpl(AlertRepository alertRepo,
                            ModuleAssignmentRepository assignmentRepo,
                            ProductionBatchRepository batchRepo,
                            BillRepository billRepo,
                            @Lazy ProductionFlowService flowService) {
        this.alertRepo = alertRepo;
        this.assignmentRepo = assignmentRepo;
        this.batchRepo = batchRepo;
        this.billRepo = billRepo;
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
                flowService.getBatchFlowStatus(batch.getBatchId());
            } catch (Exception e) {
                System.err.println("Flow check failed for batch "
                        + batch.getBatchId() + ": " + e.getMessage());
            }
        });

        // --- Check 3: Overdue customer payments ---
        LocalDateTime paymentCutoff = LocalDateTime.now().minusDays(PAYMENT_OVERDUE_DAYS);
        List<Bill> overdueBills = billRepo.findOverdueBills(paymentCutoff);
        for (Bill bill : overdueBills) {
            Long customerId = bill.getCustomer().getCustomerId();
            boolean alreadyOpen = alertRepo.findByRelatedEntityTypeAndRelatedEntityId("CUSTOMER", customerId).stream()
                    .anyMatch(a -> "open".equals(a.getStatus()) && "PAYMENT_OVERDUE".equals(a.getAlertType()));

            if (!alreadyOpen) {
                long days = bill.getBillDate() != null
                        ? ChronoUnit.DAYS.between(bill.getBillDate(), LocalDateTime.now())
                        : PAYMENT_OVERDUE_DAYS;
                String customerName = bill.getCustomer().getFirstName() + " "
                        + bill.getCustomer().getLastName();
                alertRepo.save(Alert.builder()
                        .alertType("PAYMENT_OVERDUE")
                        .message("Payment overdue: " + customerName
                                + " has unpaid bills older than " + days + " days.")
                        .relatedEntityType("CUSTOMER")
                        .relatedEntityId(customerId)
                        .status("open")
                        .build());
            }
        }
    }
}
