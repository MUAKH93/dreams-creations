package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateFinanceBankAccountRequest {
    private String accountName;
    private String bankName;
    private String accountNumber;
    private Long glAccountId;
    private BigDecimal openingBalance;
    private LocalDate openingBalanceDate;
    private String notes;
}
