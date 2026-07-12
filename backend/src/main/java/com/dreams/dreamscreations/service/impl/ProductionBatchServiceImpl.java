package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.BatchUpdateRequest;
import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.ModuleAssignmentRepository;
import com.dreams.dreamscreations.repository.ProductionBatchRepository;
import com.dreams.dreamscreations.repository.SuitRepository;
import com.dreams.dreamscreations.service.ActivityLogService;
import com.dreams.dreamscreations.service.ProductionBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductionBatchServiceImpl implements ProductionBatchService {

    private final ProductionBatchRepository batchRepo;
    private final SuitRepository suitRepo;
    private final ModuleAssignmentRepository assignmentRepo;
    private final ActivityLogService activityLogService;

    public ProductionBatchServiceImpl(ProductionBatchRepository batchRepo,
                                      SuitRepository suitRepo,
                                      ModuleAssignmentRepository assignmentRepo,
                                      ActivityLogService activityLogService) {
        this.batchRepo = batchRepo;
        this.suitRepo = suitRepo;
        this.assignmentRepo = assignmentRepo;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional
    public ProductionBatch save(ProductionBatch batch) {
        if (batch.getSuit() != null && batch.getSuit().getSuitId() != null) {
            Suit suit = suitRepo.findById(batch.getSuit().getSuitId())
                    .orElseThrow(() -> new RuntimeException("Suit not found: " + batch.getSuit().getSuitId()));
            batch.setSuit(suit);
        }

        if (batch.getBatchNumber() == null || batch.getBatchNumber().isBlank()) {
            batch.setBatchNumber(generateBatchNumber());
        }

        if (batch.getTotalSuitProduced() == null) {
            batch.setTotalSuitProduced(0);
        }

        if (batch.getStatus() == null || batch.getStatus().isBlank()) {
            batch.setStatus("planned");
        }

        return batchRepo.save(batch);
    }

    private String generateBatchNumber() {
        long next = batchRepo.count() + 1;
        return String.format("BATCH-%d-%03d", LocalDate.now().getYear(), next);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProductionBatch> getAll() { return batchRepo.findAll(); }
    @Override public List<ProductionBatch> getByStatus(String status) { return batchRepo.findByStatus(status); }

    @Override
    public ProductionBatch getById(Long id) {
        return batchRepo.findById(id).orElseThrow(() -> new RuntimeException("Batch not found: " + id));
    }

    @Override
    public List<ProductionBatch> getBySuitId(Long suitId) {
        Suit suit = suitRepo.findById(suitId).orElseThrow(() -> new RuntimeException("Suit not found: " + suitId));
        return batchRepo.findBySuit(suit);
    }

    @Override
    public ProductionBatch update(Long id, ProductionBatch updated) {
        return updateBatch(id, toUpdateRequest(updated), null);
    }

    @Override
    @Transactional
    public ProductionBatch updateBatch(Long id, BatchUpdateRequest request, User user) {
        ProductionBatch existing = getById(id);
        assertEditable(existing);

        if (request.getTotalSuitPlanned() != null) {
            if (request.getTotalSuitPlanned() < existing.getTotalSuitProduced()) {
                throw new RuntimeException(
                        "Planned quantity cannot be less than already produced ("
                                + existing.getTotalSuitProduced() + ")");
            }
            existing.setTotalSuitPlanned(request.getTotalSuitPlanned());
        }
        if (request.getExpectedCompletionDate() != null) {
            existing.setExpectedCompletionDate(request.getExpectedCompletionDate());
        }

        ProductionBatch saved = batchRepo.save(existing);
        activityLogService.log(user, "BATCH_UPDATED", "PRODUCTION_BATCH", id,
                "Updated batch " + saved.getBatchNumber());
        return saved;
    }

    @Override
    @Transactional
    public ProductionBatch cancelBatch(Long id, User user) {
        ProductionBatch existing = getById(id);
        if ("cancelled".equalsIgnoreCase(existing.getStatus())) {
            throw new RuntimeException("Batch is already cancelled");
        }
        if ("completed".equalsIgnoreCase(existing.getStatus())) {
            throw new RuntimeException("Cannot cancel a completed batch");
        }

        boolean hasActiveDispatches = assignmentRepo.findByBatch(existing).stream()
                .anyMatch(a -> !"returned".equalsIgnoreCase(a.getStatus()));
        if (hasActiveDispatches) {
            throw new RuntimeException(
                    "Cannot cancel batch with active dispatches. Wait for returns or resolve assignments first.");
        }

        existing.setStatus("cancelled");
        ProductionBatch saved = batchRepo.save(existing);
        activityLogService.log(user, "BATCH_CANCELLED", "PRODUCTION_BATCH", id,
                "Cancelled batch " + saved.getBatchNumber());
        return saved;
    }

    private void assertEditable(ProductionBatch batch) {
        if ("cancelled".equalsIgnoreCase(batch.getStatus())) {
            throw new RuntimeException("Cannot edit a cancelled batch");
        }
        if ("completed".equalsIgnoreCase(batch.getStatus())) {
            throw new RuntimeException("Cannot edit a completed batch");
        }
    }

    private BatchUpdateRequest toUpdateRequest(ProductionBatch updated) {
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setTotalSuitPlanned(updated.getTotalSuitPlanned());
        request.setExpectedCompletionDate(updated.getExpectedCompletionDate());
        return request;
    }

    @Override public void delete(Long id) { batchRepo.delete(getById(id)); }
}
