package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ApAgingReportDTO {
    private List<ApAgingLineDTO> lines;
    private BigDecimal totalCurrent;
    private BigDecimal totalDays31to60;
    private BigDecimal totalDays61to90;
    private BigDecimal totalOver90;
    private BigDecimal grandTotal;
    private BigDecimal ledgerApBalance;
    private BigDecimal difference;
    private boolean reconciled;
    private String message;
}
