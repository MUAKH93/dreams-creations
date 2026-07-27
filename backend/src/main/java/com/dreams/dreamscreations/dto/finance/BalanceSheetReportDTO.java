package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BalanceSheetReportDTO {
    private LocalDate asOfDate;
    private List<BalanceSheetLineDTO> assetLines;
    private List<BalanceSheetLineDTO> liabilityLines;
    private List<BalanceSheetLineDTO> equityLines;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private BigDecimal difference;
    private boolean balanced;
    private String message;
}
