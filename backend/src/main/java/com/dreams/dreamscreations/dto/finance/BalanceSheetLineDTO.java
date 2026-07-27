package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceSheetLineDTO {
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String section;
    private BigDecimal balance;
}
