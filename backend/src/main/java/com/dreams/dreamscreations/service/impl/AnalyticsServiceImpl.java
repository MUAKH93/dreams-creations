package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.AnalyticsDashboardDTO;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.service.AnalyticsService;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ModuleAssignmentRepository assignmentRepo;
    private final BillItemRepository billItemRepo;
    private final BillRepository billRepo;
    private final ProductionBatchRepository batchRepo;
    private final DesignRepository designRepo;
    private final InventoryService inventoryService;

    public AnalyticsServiceImpl(ModuleAssignmentRepository assignmentRepo,
                                BillItemRepository billItemRepo,
                                BillRepository billRepo,
                                ProductionBatchRepository batchRepo,
                                DesignRepository designRepo,
                                InventoryService inventoryService) {
        this.assignmentRepo = assignmentRepo;
        this.billItemRepo = billItemRepo;
        this.billRepo = billRepo;
        this.batchRepo = batchRepo;
        this.designRepo = designRepo;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDashboardDTO getDashboard(int months) {
        int period = Math.max(1, Math.min(months, 24));
        LocalDateTime since = LocalDateTime.now().minusMonths(period).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        return AnalyticsDashboardDTO.builder()
                .supervisorPerformance(buildSupervisorPerformance(since, now))
                .topDesigns(buildTopDesigns(since, 10))
                .topCustomers(buildTopCustomers(since, 10))
                .productionByMonth(buildProductionByMonth(period))
                .designProfitability(buildDesignProfitability(since))
                .stockTurnoverRatio(calculateStockTurnover(since))
                .build();
    }

    private List<AnalyticsDashboardDTO.SupervisorPerformanceDTO> buildSupervisorPerformance(
            LocalDateTime since, LocalDateTime now) {

        Map<Long, Metrics> bySupervisor = new HashMap<>();

        for (ModuleAssignment ma : assignmentRepo.findAllWithSupervisor()) {
            if (ma.getStartDate() != null && ma.getStartDate().isBefore(since)) {
                continue;
            }
            Supervisor sup = ma.getSupervisor();
            if (sup == null) continue;

            Metrics m = bySupervisor.computeIfAbsent(sup.getSupervisorId(), id -> new Metrics(sup));
            m.assignmentsTotal++;
            m.piecesSent += safeInt(ma.getQuantitySent());
            m.piecesReturnedOk += safeInt(ma.getQuantityReturnedOk());
            m.piecesDamaged += safeInt(ma.getQuantityDamaged());
            m.piecesMissing += safeInt(ma.getQuantityMissing());

            if ("returned".equalsIgnoreCase(ma.getStatus())) {
                m.completedCount++;
                if (ma.getCompletionDate() != null && ma.getDueDate() != null
                        && !ma.getCompletionDate().isAfter(ma.getDueDate())) {
                    m.onTimeCount++;
                }
            } else if (ma.getDueDate() != null && ma.getDueDate().isBefore(now)) {
                m.overdueActiveCount++;
            }
        }

        return bySupervisor.values().stream()
                .map(Metrics::toDto)
                .sorted(Comparator.comparing(AnalyticsDashboardDTO.SupervisorPerformanceDTO::getPiecesReturnedOk).reversed())
                .collect(Collectors.toList());
    }

    private List<AnalyticsDashboardDTO.DesignSalesDTO> buildTopDesigns(LocalDateTime since, int limit) {
        Map<Long, DesignAgg> agg = new HashMap<>();
        for (BillItem item : billItemRepo.findActiveItemsSince(since)) {
            Design design = item.getProduct().getSuit().getDesign();
            DesignAgg d = agg.computeIfAbsent(design.getDesignId(), id -> new DesignAgg(design));
            d.units += safeInt(item.getQuantity());
            d.revenue = d.revenue.add(item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO);
        }
        return agg.values().stream()
                .sorted(Comparator.comparing((DesignAgg d) -> d.revenue).reversed())
                .limit(limit)
                .map(DesignAgg::toSalesDto)
                .collect(Collectors.toList());
    }

    private List<AnalyticsDashboardDTO.CustomerSalesDTO> buildTopCustomers(LocalDateTime since, int limit) {
        Map<Long, CustomerAgg> agg = new HashMap<>();
        for (Bill bill : billRepo.findActiveBillsSince(since)) {
            Customer c = bill.getCustomer();
            CustomerAgg ca = agg.computeIfAbsent(c.getCustomerId(), id -> new CustomerAgg(c));
            ca.billCount++;
            ca.revenue = ca.revenue.add(bill.getFinalAmount() != null ? bill.getFinalAmount() : BigDecimal.ZERO);
        }
        return agg.values().stream()
                .sorted(Comparator.comparing((CustomerAgg c) -> c.revenue).reversed())
                .limit(limit)
                .map(CustomerAgg::toDto)
                .collect(Collectors.toList());
    }

    private List<AnalyticsDashboardDTO.MonthlyProductionPoint> buildProductionByMonth(int months) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<YearMonth, MonthlyAgg> map = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) {
            map.put(YearMonth.from(LocalDate.now().minusMonths(i)), new MonthlyAgg());
        }

        for (ProductionBatch batch : batchRepo.findAll()) {
            if (!"completed".equalsIgnoreCase(batch.getStatus())) continue;
            LocalDate ref = batch.getEndDate() != null ? batch.getEndDate()
                    : (batch.getStartDate() != null ? batch.getStartDate() : null);
            if (ref == null) continue;
            YearMonth ym = YearMonth.from(ref);
            MonthlyAgg agg = map.get(ym);
            if (agg == null) continue;
            agg.batchesCompleted++;
            agg.unitsProduced += safeInt(batch.getTotalSuitProduced());
        }

        return map.entrySet().stream()
                .map(e -> AnalyticsDashboardDTO.MonthlyProductionPoint.builder()
                        .month(e.getKey().format(fmt))
                        .unitsProduced(e.getValue().unitsProduced)
                        .batchesCompleted(e.getValue().batchesCompleted)
                        .build())
                .collect(Collectors.toList());
    }

    private List<AnalyticsDashboardDTO.DesignProfitabilityDTO> buildDesignProfitability(LocalDateTime since) {
        Map<Long, ProfitAgg> agg = new HashMap<>();
        for (BillItem item : billItemRepo.findActiveItemsSince(since)) {
            Design design = item.getProduct().getSuit().getDesign();
            ProfitAgg p = agg.computeIfAbsent(design.getDesignId(), id -> new ProfitAgg(design));
            int qty = safeInt(item.getQuantity());
            p.units += qty;
            p.revenue = p.revenue.add(item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO);
        }

        for (Design design : designRepo.findAll()) {
            agg.computeIfAbsent(design.getDesignId(), id -> new ProfitAgg(design));
        }

        return agg.values().stream()
                .filter(p -> p.units > 0 || p.design.getProductionCost() != null)
                .sorted(Comparator.comparing((ProfitAgg p) -> p.revenue).reversed())
                .limit(15)
                .map(ProfitAgg::toDto)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateStockTurnover(LocalDateTime since) {
        long unitsSold = billItemRepo.findActiveItemsSince(since).stream()
                .mapToLong(i -> safeInt(i.getQuantity()))
                .sum();
        long stockUnits = inventoryService.getAllStock().stream()
                .mapToLong(i -> i.getQuantity() != null ? i.getQuantity() : 0)
                .sum();
        if (stockUnits <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(unitsSold)
                .divide(BigDecimal.valueOf(stockUnits), 2, RoundingMode.HALF_UP);
    }

    private static int safeInt(Integer v) {
        return v != null ? v : 0;
    }

    private static BigDecimal rate(long part, long total) {
        if (total <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(part * 100.0 / total).setScale(1, RoundingMode.HALF_UP);
    }

    private static class Metrics {
        final Supervisor supervisor;
        long assignmentsTotal;
        long completedCount;
        long onTimeCount;
        long overdueActiveCount;
        long piecesSent;
        long piecesReturnedOk;
        long piecesDamaged;
        long piecesMissing;

        Metrics(Supervisor supervisor) { this.supervisor = supervisor; }

        AnalyticsDashboardDTO.SupervisorPerformanceDTO toDto() {
            String name = (supervisor.getFirstName() + " " + (supervisor.getLastName() != null ? supervisor.getLastName() : "")).trim();
            return AnalyticsDashboardDTO.SupervisorPerformanceDTO.builder()
                    .supervisorId(supervisor.getSupervisorId())
                    .supervisorName(name)
                    .assignmentsTotal(assignmentsTotal)
                    .completedCount(completedCount)
                    .onTimeCount(onTimeCount)
                    .onTimeRate(rate(onTimeCount, completedCount))
                    .overdueActiveCount(overdueActiveCount)
                    .piecesSent(piecesSent)
                    .piecesReturnedOk(piecesReturnedOk)
                    .piecesDamaged(piecesDamaged)
                    .piecesMissing(piecesMissing)
                    .damageRate(rate(piecesDamaged, piecesSent))
                    .missingRate(rate(piecesMissing, piecesSent))
                    .yieldRate(rate(piecesReturnedOk, piecesSent))
                    .build();
        }
    }

    private static class DesignAgg {
        final Design design;
        long units;
        BigDecimal revenue = BigDecimal.ZERO;

        DesignAgg(Design design) { this.design = design; }

        AnalyticsDashboardDTO.DesignSalesDTO toSalesDto() {
            return AnalyticsDashboardDTO.DesignSalesDTO.builder()
                    .designId(design.getDesignId())
                    .designCode(design.getDesignCode())
                    .name(design.getName())
                    .unitsSold(units)
                    .revenue(revenue)
                    .build();
        }
    }

    private static class CustomerAgg {
        final Customer customer;
        long billCount;
        BigDecimal revenue = BigDecimal.ZERO;

        CustomerAgg(Customer customer) { this.customer = customer; }

        AnalyticsDashboardDTO.CustomerSalesDTO toDto() {
            String name = (customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName() : "")).trim();
            return AnalyticsDashboardDTO.CustomerSalesDTO.builder()
                    .customerId(customer.getCustomerId())
                    .customerName(name)
                    .billCount(billCount)
                    .revenue(revenue)
                    .build();
        }
    }

    private static class MonthlyAgg {
        long unitsProduced;
        long batchesCompleted;
    }

    private static class ProfitAgg {
        final Design design;
        long units;
        BigDecimal revenue = BigDecimal.ZERO;

        ProfitAgg(Design design) { this.design = design; }

        AnalyticsDashboardDTO.DesignProfitabilityDTO toDto() {
            BigDecimal costPerUnit = design.getProductionCost() != null
                    ? design.getProductionCost() : BigDecimal.ZERO;
            BigDecimal totalCost = costPerUnit.multiply(BigDecimal.valueOf(units));
            BigDecimal profit = revenue.subtract(totalCost);
            BigDecimal margin = revenue.signum() > 0
                    ? profit.multiply(BigDecimal.valueOf(100)).divide(revenue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal avgPrice = units > 0
                    ? revenue.divide(BigDecimal.valueOf(units), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return AnalyticsDashboardDTO.DesignProfitabilityDTO.builder()
                    .designId(design.getDesignId())
                    .designCode(design.getDesignCode())
                    .name(design.getName())
                    .unitsSold(units)
                    .revenue(revenue)
                    .productionCostPerUnit(costPerUnit)
                    .totalCost(totalCost)
                    .profit(profit)
                    .profitMarginPercent(margin)
                    .avgSellingPrice(avgPrice)
                    .build();
        }
    }
}
