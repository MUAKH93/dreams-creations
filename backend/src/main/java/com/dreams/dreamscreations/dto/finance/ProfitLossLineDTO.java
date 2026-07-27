package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProfitLossLineDTO {
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private BigDecimal amount;
}
