package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinanceBankAccountDTO {
    private Long bankAccountId;
    private String accountName;
    private String bankName;
    private String accountNumber;
    private Long glAccountId;
    private String glAccountCode;
    private String glAccountName;
    private BigDecimal openingBalance;
    private LocalDate openingBalanceDate;
    private Boolean isActive;
    private String notes;
}
