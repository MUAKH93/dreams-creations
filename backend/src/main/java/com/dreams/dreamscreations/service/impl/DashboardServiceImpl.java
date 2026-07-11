package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.DashboardSummaryDTO;
import com.dreams.dreamscreations.dto.InventoryItemDTO;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.repository.AlertRepository;
import com.dreams.dreamscreations.repository.BillRepository;
import com.dreams.dreamscreations.repository.CustomerBalanceRepository;
import com.dreams.dreamscreations.repository.ModuleAssignmentRepository;
import com.dreams.dreamscreations.repository.ProductRepository;
import com.dreams.dreamscreations.repository.ProductionBatchRepository;
import com.dreams.dreamscreations.service.DashboardService;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final AlertRepository alertRepo;
    private final ProductionBatchRepository batchRepo;
    private final ModuleAssignmentRepository assignmentRepo;
    private final BillRepository billRepo;
    private final CustomerBalanceRepository balanceRepo;
    private final InventoryService inventoryService;
    private final ProductRepository productRepo;

    public DashboardServiceImpl(AlertRepository alertRepo,
                                ProductionBatchRepository batchRepo,
                                ModuleAssignmentRepository assignmentRepo,
                                BillRepository billRepo,
                                CustomerBalanceRepository balanceRepo,
                                InventoryService inventoryService,
                                ProductRepository productRepo) {
        this.alertRepo = alertRepo;
        this.batchRepo = batchRepo;
        this.assignmentRepo = assignmentRepo;
        this.billRepo = billRepo;
        this.balanceRepo = balanceRepo;
        this.inventoryService = inventoryService;
        this.productRepo = productRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        Map<Long, BigDecimal> priceBySuit = productRepo.findAllActiveWithSuitDetails().stream()
                .collect(Collectors.toMap(
                        p -> p.getSuit().getSuitId(),
                        this::unitPrice,
                        (a, b) -> a));

        long totalUnits = 0;
        long lowStock = 0;
        BigDecimal stockValue = BigDecimal.ZERO;

        for (InventoryItemDTO item : inventoryService.getAllStock()) {
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            totalUnits += qty;
            if (qty <= InventoryServiceImpl.LOW_STOCK_THRESHOLD) {
                lowStock++;
            }
            BigDecimal price = priceBySuit.getOrDefault(item.getSuitId(), BigDecimal.ZERO);
            stockValue = stockValue.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal outstanding = balanceRepo.findAll().stream()
                .map(b -> b.getBalance() != null ? b.getBalance() : BigDecimal.ZERO)
                .filter(b -> b.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryDTO.builder()
                .openAlerts(alertRepo.findByStatus("open").size())
                .lowStockItems(lowStock)
                .batchesInProgress(batchRepo.findByStatus("in_progress").size())
                .overdueDispatches(assignmentRepo.findOverdueAssignments(LocalDateTime.now()).size())
                .completedBatches(batchRepo.findByStatus("completed").size())
                .unpaidBills(billRepo.countByStatus("unpaid"))
                .partialBills(billRepo.countByStatus("partial"))
                .totalStockUnits(totalUnits)
                .estimatedStockValue(stockValue)
                .totalOutstandingBalance(outstanding)
                .build();
    }

    private BigDecimal unitPrice(Product product) {
        BigDecimal selling = product.getSellingPrice();
        if (selling != null && selling.signum() > 0) {
            return selling;
        }
        if (product.getSuit().getDesign().getBasePrice() != null) {
            return product.getSuit().getDesign().getBasePrice();
        }
        return BigDecimal.ZERO;
    }
}
