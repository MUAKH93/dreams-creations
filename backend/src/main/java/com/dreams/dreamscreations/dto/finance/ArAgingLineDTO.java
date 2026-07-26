package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ArAgingLineDTO {
    private Long customerId;
    private String customerName;
    private String phone;
    private BigDecimal current;
    private BigDecimal days31to60;
    private BigDecimal days61to90;
    private BigDecimal over90;
    private BigDecimal totalOutstanding;
    private BigDecimal operationalBalance;
}
