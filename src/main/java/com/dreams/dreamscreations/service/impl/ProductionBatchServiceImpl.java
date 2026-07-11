package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.repository.ProductionBatchRepository;
import com.dreams.dreamscreations.repository.SuitRepository;
import com.dreams.dreamscreations.service.ProductionBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductionBatchServiceImpl implements ProductionBatchService {

    private final ProductionBatchRepository batchRepo;
    private final SuitRepository suitRepo;

    public ProductionBatchServiceImpl(ProductionBatchRepository batchRepo, SuitRepository suitRepo) {
        this.batchRepo = batchRepo;
        this.suitRepo = suitRepo;
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
        ProductionBatch existing = getById(id);
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setExpectedCompletionDate(updated.getExpectedCompletionDate());
        existing.setStatus(updated.getStatus());
        existing.setTotalSuitPlanned(updated.getTotalSuitPlanned());
        existing.setTotalSuitProduced(updated.getTotalSuitProduced());
        return batchRepo.save(existing);
    }

    @Override public void delete(Long id) { batchRepo.delete(getById(id)); }
}
