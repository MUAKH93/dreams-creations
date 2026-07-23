package com.dreams.dreamscreations.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceJournalEntryDTO {

    private Long entryId;
    private String entryNumber;
    private LocalDate entryDate;
    private String memo;
    private String sourceType;
    private String status;
    private LocalDateTime postedAt;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private List<FinanceJournalLineDTO> lines;
}
