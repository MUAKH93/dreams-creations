package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateFinanceJournalLineRequest {

    private Long accountId;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String lineMemo;
}
