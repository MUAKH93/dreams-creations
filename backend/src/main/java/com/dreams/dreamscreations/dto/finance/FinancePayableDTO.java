package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinancePayableDTO {
    private Long payableId;
    private Long vendorId;
    private String vendorName;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private Long expenseAccountId;
    private String expenseAccountCode;
    private String expenseAccountName;
    private String memo;
    private String status;
}
