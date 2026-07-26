package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ArAgingReportDTO {
    private List<ArAgingLineDTO> lines;
    private BigDecimal totalCurrent;
    private BigDecimal totalDays31to60;
    private BigDecimal totalDays61to90;
    private BigDecimal totalOver90;
    private BigDecimal grandTotal;
}
