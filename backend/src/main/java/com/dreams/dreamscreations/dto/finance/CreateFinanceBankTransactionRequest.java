package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateFinanceBankTransactionRequest {
    private Long bankAccountId;
    private LocalDate transactionDate;
    private String description;
    private BigDecimal amount;
    private String referenceNo;
}
