package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinanceBankTransactionDTO {
    private Long transactionId;
    private Long bankAccountId;
    private LocalDate transactionDate;
    private String description;
    private BigDecimal amount;
    private String referenceNo;
    private Boolean isReconciled;
    private Long matchedEntryId;
    private String matchedEntryNumber;
}
