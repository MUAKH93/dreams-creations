package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApAgingLineDTO {
    private Long vendorId;
    private String vendorName;
    private String phone;
    private BigDecimal current;
    private BigDecimal days31to60;
    private BigDecimal days61to90;
    private BigDecimal over90;
    private BigDecimal totalOutstanding;
}
