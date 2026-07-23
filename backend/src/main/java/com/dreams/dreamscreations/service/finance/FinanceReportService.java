package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.GeneralLedgerReportDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceReportDTO;

import java.time.LocalDate;

public interface FinanceReportService {

    TrialBalanceReportDTO getTrialBalance(boolean activeOnly, boolean includeZero);

    GeneralLedgerReportDTO getGeneralLedger(Long accountId, LocalDate fromDate, LocalDate toDate);
}
