package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateFinancePayableRequest {
    private Long vendorId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private Long expenseAccountId;
    private String memo;
}
