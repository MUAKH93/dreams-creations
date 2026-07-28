package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BankReconciliationReportDTO {
    private Long bankAccountId;
    private String accountName;
    private String bankName;
    private Long glAccountId;
    private String glAccountCode;
    private LocalDate asOfDate;
    private BigDecimal openingBalance;
    private BigDecimal statementBalance;
    private BigDecimal ledgerBalance;
    private BigDecimal unreconciledStatementTotal;
    private int unreconciledStatementCount;
    private BigDecimal difference;
    private boolean reconciled;
    private String message;
    private List<BankReconciliationLineDTO> statementLines;
    private List<BankReconciliationBookLineDTO> bookLines;
}
