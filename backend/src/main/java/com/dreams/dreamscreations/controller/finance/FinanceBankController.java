package com.dreams.dreamscreations.controller.finance;

import com.dreams.dreamscreations.dto.finance.*;
import com.dreams.dreamscreations.service.finance.FinanceBankService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance/bank")
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceBankController {

    private final FinanceBankService bankService;

    public FinanceBankController(FinanceBankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<FinanceBankAccountDTO>> getAccounts(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(bankService.getBankAccounts(activeOnly));
    }

    @PostMapping("/accounts")
    public ResponseEntity<FinanceBankAccountDTO> createAccount(
            @RequestBody CreateFinanceBankAccountRequest request) {
        return ResponseEntity.ok(bankService.createBankAccount(request));
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<FinanceBankTransactionDTO>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(bankService.getTransactions(id));
    }

    @PostMapping("/transactions")
    public ResponseEntity<FinanceBankTransactionDTO> createTransaction(
            @RequestBody CreateFinanceBankTransactionRequest request) {
        return ResponseEntity.ok(bankService.createTransaction(request));
    }

    @PostMapping("/transactions/{id}/reconcile")
    public ResponseEntity<FinanceBankTransactionDTO> reconcileTransaction(
            @PathVariable Long id,
            @RequestBody(required = false) ReconcileBankTransactionRequest request) {
        return ResponseEntity.ok(bankService.reconcileTransaction(id, request));
    }

    @GetMapping("/accounts/{id}/reconciliation")
    public ResponseEntity<BankReconciliationReportDTO> getReconciliation(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ResponseEntity.ok(bankService.getReconciliation(id, asOfDate));
    }
}
