package com.dreams.dreamscreations.dto.finance;

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
public class TrialBalanceReportDTO {

    private List<TrialBalanceLineDTO> lines;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private boolean balanced;
}
