package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateFinanceJournalRequest {

    private LocalDate entryDate;
    private String memo;
    private List<CreateFinanceJournalLineRequest> lines;
}
