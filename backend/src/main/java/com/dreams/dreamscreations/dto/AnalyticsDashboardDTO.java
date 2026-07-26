package com.dreams.dreamscreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDashboardDTO {

    private List<SupervisorPerformanceDTO> supervisorPerformance;
    private List<DesignSalesDTO> topDesigns;
    private List<CustomerSalesDTO> topCustomers;
    private List<MonthlyProductionPoint> productionByMonth;
    private List<DesignProfitabilityDTO> designProfitability;
    private BigDecimal stockTurnoverRatio;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupervisorPerformanceDTO {
        private Long supervisorId;
        private String supervisorName;
        private long assignmentsTotal;
        private long completedCount;
        private long onTimeCount;
        private BigDecimal onTimeRate;
        private long overdueActiveCount;
        private long piecesSent;
        private long piecesReturnedOk;
        private long piecesDamaged;
        private long piecesMissing;
        private BigDecimal damageRate;
        private BigDecimal missingRate;
        private BigDecimal yieldRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DesignSalesDTO {
        private Long designId;
        private String designCode;
        private String name;
        private long unitsSold;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerSalesDTO {
        private Long customerId;
        private String customerName;
        private long billCount;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyProductionPoint {
        private String month;
        private long unitsProduced;
        private long batchesCompleted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DesignProfitabilityDTO {
        private Long designId;
        private String designCode;
        private String name;
        private long unitsSold;
        private BigDecimal revenue;
        private BigDecimal productionCostPerUnit;
        private BigDecimal totalCost;
        private BigDecimal profit;
        private BigDecimal profitMarginPercent;
        private BigDecimal avgSellingPrice;
    }
}
