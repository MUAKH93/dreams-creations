package com.dreams.dreamscreations.controller.finance;

import com.dreams.dreamscreations.dto.finance.GeneralLedgerReportDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceReportDTO;
import com.dreams.dreamscreations.service.finance.FinanceReportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finance/reports")
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceReportController {

    private final FinanceReportService reportService;

    public FinanceReportController(FinanceReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/trial-balance")
    public ResponseEntity<TrialBalanceReportDTO> trialBalance(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "false") boolean includeZero) {
        return ResponseEntity.ok(reportService.getTrialBalance(activeOnly, includeZero));
    }

    @GetMapping("/general-ledger")
    public ResponseEntity<GeneralLedgerReportDTO> generalLedger(
            @RequestParam Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getGeneralLedger(accountId, fromDate, toDate));
    }
}
