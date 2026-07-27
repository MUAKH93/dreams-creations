package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class InventoryValuationReportDTO {
    private BigDecimal ledgerInventoryBalance;
    private BigDecimal operationalStockValue;
    private BigDecimal difference;
    private boolean reconciled;
    private int totalUnits;
    private int linesMissingCost;
    private String message;
    private List<InventoryValuationLineDTO> lines;
}
