package com.dreams.dreamscreations.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryDTO {
    private long openAlerts;
    private long lowStockItems;
    private long batchesInProgress;
    private long overdueDispatches;
    private long completedBatches;
    private long unpaidBills;
    private long partialBills;
    private long totalStockUnits;
    private BigDecimal estimatedStockValue;
    private BigDecimal totalOutstandingBalance;
}
