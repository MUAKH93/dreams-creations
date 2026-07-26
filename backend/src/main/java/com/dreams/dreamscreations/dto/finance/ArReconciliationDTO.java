package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ArReconciliationDTO {
    private BigDecimal ledgerArBalance;
    private BigDecimal operationalBalanceTotal;
    private BigDecimal difference;
    private boolean reconciled;
    private String message;
}
