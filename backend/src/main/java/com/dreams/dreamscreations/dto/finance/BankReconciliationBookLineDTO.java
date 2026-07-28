package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BankReconciliationBookLineDTO {
    private LocalDate entryDate;
    private String entryNumber;
    private String memo;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal netAmount;
    private Long entryId;
}
