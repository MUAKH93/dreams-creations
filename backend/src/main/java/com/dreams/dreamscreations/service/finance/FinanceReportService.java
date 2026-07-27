package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.ArAgingReportDTO;
import com.dreams.dreamscreations.dto.finance.ArReconciliationDTO;
import com.dreams.dreamscreations.dto.finance.BalanceSheetReportDTO;
import com.dreams.dreamscreations.dto.finance.GeneralLedgerReportDTO;
import com.dreams.dreamscreations.dto.finance.InventoryValuationReportDTO;
import com.dreams.dreamscreations.dto.finance.ProfitLossReportDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceReportDTO;

import java.time.LocalDate;

public interface FinanceReportService {

    TrialBalanceReportDTO getTrialBalance(boolean activeOnly, boolean includeZero);

    GeneralLedgerReportDTO getGeneralLedger(Long accountId, LocalDate fromDate, LocalDate toDate);

    ArAgingReportDTO getArAging();

    ArReconciliationDTO getArReconciliation();

    InventoryValuationReportDTO getInventoryValuation();

    ProfitLossReportDTO getProfitLoss(LocalDate fromDate, LocalDate toDate);

    BalanceSheetReportDTO getBalanceSheet(LocalDate asOfDate);
}
