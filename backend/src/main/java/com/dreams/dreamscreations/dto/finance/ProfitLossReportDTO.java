package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProfitLossReportDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<ProfitLossLineDTO> incomeLines;
    private List<ProfitLossLineDTO> expenseLines;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netIncome;
}
