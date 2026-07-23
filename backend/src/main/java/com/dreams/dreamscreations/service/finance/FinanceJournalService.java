package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinanceJournalRequest;
import com.dreams.dreamscreations.dto.finance.FinanceJournalEntryDTO;

import java.util.List;

public interface FinanceJournalService {

    List<FinanceJournalEntryDTO> getAll();

    FinanceJournalEntryDTO getById(Long id);

    FinanceJournalEntryDTO createManualEntry(CreateFinanceJournalRequest request, Long userId);
}
