package com.dreams.dreamscreations.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralLedgerReportDTO {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private List<GeneralLedgerLineDTO> lines;
}
