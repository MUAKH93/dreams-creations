package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.DashboardChartsDTO;
import com.dreams.dreamscreations.dto.DashboardSummaryDTO;
import com.dreams.dreamscreations.dto.InventoryItemDTO;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.repository.AlertRepository;
import com.dreams.dreamscreations.repository.BillRepository;
import com.dreams.dreamscreations.repository.CustomerBalanceRepository;
import com.dreams.dreamscreations.repository.ModuleAssignmentRepository;
import com.dreams.dreamscreations.repository.ProductRepository;
import com.dreams.dreamscreations.repository.ProductionBatchRepository;
import com.dreams.dreamscreations.repository.QuotationRepository;
import com.dreams.dreamscreations.service.DashboardService;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final QuotationRepository quotationRepo;

    public DashboardServiceImpl(AlertRepository alertRepo,
                                ProductionBatchRepository batchRepo,
                                ModuleAssignmentRepository assignmentRepo,
                                BillRepository billRepo,
                                CustomerBalanceRepository balanceRepo,
                                InventoryService inventoryService,
                                ProductRepository productRepo,
                                QuotationRepository quotationRepo) {
        this.alertRepo = alertRepo;
        this.batchRepo = batchRepo;
        this.assignmentRepo = assignmentRepo;
        this.billRepo = billRepo;
        this.balanceRepo = balanceRepo;
        this.inventoryService = inventoryService;
        this.productRepo = productRepo;
        this.quotationRepo = quotationRepo;
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
                .overduePaymentCustomers(billRepo.countCustomersWithOverdueBills(
                        java.time.LocalDateTime.now().minusDays(AlertServiceImpl.PAYMENT_OVERDUE_DAYS)))
                .paymentOverdueAlerts(alertRepo.findByStatus("open").stream()
                        .filter(a -> "PAYMENT_OVERDUE".equals(a.getAlertType()))
                        .count())
                .pendingQuotations(quotationRepo.countByStatus("submitted") + quotationRepo.countByStatus("draft"))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardChartsDTO getCharts() {
        LocalDateTime since = LocalDateTime.now().minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        Map<YearMonth, BigDecimal> salesMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(LocalDateTime.now().minusMonths(i));
            salesMap.put(ym, BigDecimal.ZERO);
        }

        for (var bill : billRepo.findActiveBillsSince(since)) {
            if (bill.getBillDate() == null) continue;
            YearMonth ym = YearMonth.from(bill.getBillDate());
            if (salesMap.containsKey(ym)) {
                BigDecimal amt = bill.getFinalAmount() != null ? bill.getFinalAmount() : BigDecimal.ZERO;
                salesMap.merge(ym, amt, BigDecimal::add);
            }
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        List<DashboardChartsDTO.MonthlySalesPoint> salesByMonth = salesMap.entrySet().stream()
                .map(e -> DashboardChartsDTO.MonthlySalesPoint.builder()
                        .month(e.getKey().format(fmt))
                        .amount(e.getValue())
                        .build())
                .collect(Collectors.toList());

        Map<String, Long> pipeline = new LinkedHashMap<>();
        pipeline.put("planned", batchRepo.findByStatus("planned").stream().count());
        pipeline.put("in_progress", batchRepo.findByStatus("in_progress").stream().count());
        pipeline.put("completed", batchRepo.findByStatus("completed").stream().count());
        pipeline.put("cancelled", batchRepo.findByStatus("cancelled").stream().count());

        return DashboardChartsDTO.builder()
                .salesByMonth(salesByMonth)
                .productionPipeline(pipeline)
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
