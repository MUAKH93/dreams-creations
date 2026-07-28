package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.*;
import com.dreams.dreamscreations.entity.finance.*;
import com.dreams.dreamscreations.repository.finance.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceBankServiceImpl implements FinanceBankService {

    private final FinanceBankAccountRepository bankAccountRepo;
    private final FinanceBankTransactionRepository transactionRepo;
    private final FinanceAccountRepository accountRepo;
    private final FinanceJournalEntryRepository entryRepo;
    private final FinanceJournalLineRepository lineRepo;

    public FinanceBankServiceImpl(FinanceBankAccountRepository bankAccountRepo,
                                    FinanceBankTransactionRepository transactionRepo,
                                    FinanceAccountRepository accountRepo,
                                    FinanceJournalEntryRepository entryRepo,
                                    FinanceJournalLineRepository lineRepo) {
        this.bankAccountRepo = bankAccountRepo;
        this.transactionRepo = transactionRepo;
        this.accountRepo = accountRepo;
        this.entryRepo = entryRepo;
        this.lineRepo = lineRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceBankAccountDTO> getBankAccounts(boolean activeOnly) {
        List<FinanceBankAccount> accounts = activeOnly
                ? bankAccountRepo.findByIsActiveTrueOrderByAccountNameAsc()
                : bankAccountRepo.findAllWithGlAccount();
        return accounts.stream().map(this::toAccountDto).toList();
    }

    @Override
    @Transactional
    public FinanceBankAccountDTO createBankAccount(CreateFinanceBankAccountRequest request) {
        validateAccountCreate(request);

        FinanceAccount glAccount = accountRepo.findById(request.getGlAccountId())
                .orElseThrow(() -> new RuntimeException("GL account not found: " + request.getGlAccountId()));
        if (!"ASSET".equalsIgnoreCase(glAccount.getAccountType())) {
            throw new RuntimeException("Bank account must link to an asset (cash) GL account");
        }

        FinanceBankAccount saved = bankAccountRepo.save(FinanceBankAccount.builder()
                .accountName(request.getAccountName().trim())
                .bankName(trimOrNull(request.getBankName()))
                .accountNumber(trimOrNull(request.getAccountNumber()))
                .glAccount(glAccount)
                .openingBalance(nz(request.getOpeningBalance()))
                .openingBalanceDate(request.getOpeningBalanceDate())
                .notes(trimOrNull(request.getNotes()))
                .isActive(true)
                .build());

        return toAccountDto(bankAccountRepo.findByIdWithGlAccount(saved.getBankAccountId()).orElse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceBankTransactionDTO> getTransactions(Long bankAccountId) {
        requireBankAccount(bankAccountId);
        return transactionRepo.findByBankAccountId(bankAccountId).stream()
                .map(this::toTransactionDto)
                .toList();
    }

    @Override
    @Transactional
    public FinanceBankTransactionDTO createTransaction(CreateFinanceBankTransactionRequest request) {
        validateTransactionCreate(request);
        FinanceBankAccount bankAccount = requireBankAccount(request.getBankAccountId());

        FinanceBankTransaction saved = transactionRepo.save(FinanceBankTransaction.builder()
                .bankAccount(bankAccount)
                .transactionDate(request.getTransactionDate())
                .description(trimOrNull(request.getDescription()))
                .amount(request.getAmount())
                .referenceNo(trimOrNull(request.getReferenceNo()))
                .isReconciled(false)
                .build());

        return toTransactionDto(saved);
    }

    @Override
    @Transactional
    public FinanceBankTransactionDTO reconcileTransaction(Long transactionId,
                                                          ReconcileBankTransactionRequest request) {
        FinanceBankTransaction tx = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Bank transaction not found: " + transactionId));

        if (Boolean.TRUE.equals(tx.getIsReconciled())) {
            throw new RuntimeException("Transaction is already reconciled");
        }

        FinanceJournalEntry matchedEntry = null;
        if (request != null && request.getMatchedEntryId() != null) {
            matchedEntry = entryRepo.findById(request.getMatchedEntryId())
                    .orElseThrow(() -> new RuntimeException("Journal entry not found: " + request.getMatchedEntryId()));
        }

        tx.setIsReconciled(true);
        tx.setReconciledAt(LocalDateTime.now());
        tx.setMatchedEntry(matchedEntry);

        return toTransactionDto(transactionRepo.save(tx));
    }

    @Override
    @Transactional(readOnly = true)
    public BankReconciliationReportDTO getReconciliation(Long bankAccountId, LocalDate asOfDate) {
        if (asOfDate == null) {
            throw new RuntimeException("As-of date is required");
        }

        FinanceBankAccount bankAccount = bankAccountRepo.findByIdWithGlAccount(bankAccountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found: " + bankAccountId));

        FinanceAccount glAccount = bankAccount.getGlAccount();
        BigDecimal opening = nz(bankAccount.getOpeningBalance());

        List<FinanceBankTransaction> transactions =
                transactionRepo.findByBankAccountIdAsOf(bankAccountId, asOfDate);

        BigDecimal statementBalance = opening;
        BigDecimal unreconciledTotal = BigDecimal.ZERO;
        int unreconciledCount = 0;
        List<BankReconciliationLineDTO> statementLines = new ArrayList<>();

        for (FinanceBankTransaction tx : transactions) {
            statementBalance = statementBalance.add(nz(tx.getAmount()));
            if (!Boolean.TRUE.equals(tx.getIsReconciled())) {
                unreconciledTotal = unreconciledTotal.add(nz(tx.getAmount()));
                unreconciledCount++;
            }
            statementLines.add(BankReconciliationLineDTO.builder()
                    .transactionId(tx.getTransactionId())
                    .transactionDate(tx.getTransactionDate())
                    .description(tx.getDescription())
                    .amount(tx.getAmount())
                    .referenceNo(tx.getReferenceNo())
                    .isReconciled(tx.getIsReconciled())
                    .matchedEntryId(tx.getMatchedEntry() != null ? tx.getMatchedEntry().getEntryId() : null)
                    .matchedEntryNumber(tx.getMatchedEntry() != null ? tx.getMatchedEntry().getEntryNumber() : null)
                    .build());
        }

        BigDecimal ledgerBalance = computeLedgerBalance(glAccount, asOfDate);
        BigDecimal difference = ledgerBalance.subtract(statementBalance);
        boolean reconciled = difference.abs().compareTo(new BigDecimal("0.01")) <= 0
                && unreconciledCount == 0;

        String message;
        if (reconciled) {
            message = "Bank statement matches the ledger cash balance.";
        } else if (unreconciledCount > 0) {
            message = unreconciledCount + " unreconciled statement line(s) — mark them reconciled after matching to journal entries.";
        } else {
            message = "Difference detected — review opening balance, missing statement lines, or unposted journal entries.";
        }

        List<BankReconciliationBookLineDTO> bookLines = loadBookLines(glAccount.getAccountId(), asOfDate);

        return BankReconciliationReportDTO.builder()
                .bankAccountId(bankAccount.getBankAccountId())
                .accountName(bankAccount.getAccountName())
                .bankName(bankAccount.getBankName())
                .glAccountId(glAccount.getAccountId())
                .glAccountCode(glAccount.getAccountCode())
                .asOfDate(asOfDate)
                .openingBalance(opening)
                .statementBalance(statementBalance)
                .ledgerBalance(ledgerBalance)
                .unreconciledStatementTotal(unreconciledTotal)
                .unreconciledStatementCount(unreconciledCount)
                .difference(difference)
                .reconciled(reconciled)
                .message(message)
                .statementLines(statementLines)
                .bookLines(bookLines)
                .build();
    }

    private List<BankReconciliationBookLineDTO> loadBookLines(Long accountId, LocalDate asOfDate) {
        LocalDate fromDate = asOfDate.minusMonths(3);
        if (fromDate.isBefore(LocalDate.of(2000, 1, 1))) {
            fromDate = LocalDate.of(2000, 1, 1);
        }

        List<BankReconciliationBookLineDTO> lines = new ArrayList<>();
        for (FinanceJournalLineRepository.GeneralLedgerProjection row :
                lineRepo.generalLedger(accountId, fromDate, asOfDate)) {
            BigDecimal debit = nz(row.getDebitAmount());
            BigDecimal credit = nz(row.getCreditAmount());
            lines.add(BankReconciliationBookLineDTO.builder()
                    .entryDate(row.getEntryDate())
                    .entryNumber(row.getEntryNumber())
                    .memo(row.getLineMemo() != null ? row.getLineMemo() : row.getEntryMemo())
                    .debitAmount(debit)
                    .creditAmount(credit)
                    .netAmount(debit.subtract(credit))
                    .entryId(row.getEntryId())
                    .build());
        }
        return lines;
    }

    private BigDecimal computeLedgerBalance(FinanceAccount account, LocalDate asOfDate) {
        List<FinanceJournalLineRepository.TrialBalanceProjection> rows =
                lineRepo.balanceSheetAsOf(asOfDate);
        return rows.stream()
                .filter(row -> account.getAccountId().equals(row.getAccountId()))
                .map(row -> nz(row.getTotalDebit()).subtract(nz(row.getTotalCredit())))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private FinanceBankAccount requireBankAccount(Long id) {
        return bankAccountRepo.findByIdWithGlAccount(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found: " + id));
    }

    private void validateAccountCreate(CreateFinanceBankAccountRequest request) {
        if (request == null || request.getAccountName() == null || request.getAccountName().isBlank()) {
            throw new RuntimeException("Account name is required");
        }
        if (request.getGlAccountId() == null) {
            throw new RuntimeException("GL account is required");
        }
    }

    private void validateTransactionCreate(CreateFinanceBankTransactionRequest request) {
        if (request == null || request.getBankAccountId() == null) {
            throw new RuntimeException("Bank account is required");
        }
        if (request.getTransactionDate() == null) {
            throw new RuntimeException("Transaction date is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Amount must be non-zero");
        }
    }

    private FinanceBankAccountDTO toAccountDto(FinanceBankAccount account) {
        FinanceAccount gl = account.getGlAccount();
        return FinanceBankAccountDTO.builder()
                .bankAccountId(account.getBankAccountId())
                .accountName(account.getAccountName())
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .glAccountId(gl != null ? gl.getAccountId() : null)
                .glAccountCode(gl != null ? gl.getAccountCode() : null)
                .glAccountName(gl != null ? gl.getAccountName() : null)
                .openingBalance(account.getOpeningBalance())
                .openingBalanceDate(account.getOpeningBalanceDate())
                .isActive(account.getIsActive())
                .notes(account.getNotes())
                .build();
    }

    private FinanceBankTransactionDTO toTransactionDto(FinanceBankTransaction tx) {
        return FinanceBankTransactionDTO.builder()
                .transactionId(tx.getTransactionId())
                .bankAccountId(tx.getBankAccount() != null ? tx.getBankAccount().getBankAccountId() : null)
                .transactionDate(tx.getTransactionDate())
                .description(tx.getDescription())
                .amount(tx.getAmount())
                .referenceNo(tx.getReferenceNo())
                .isReconciled(tx.getIsReconciled())
                .matchedEntryId(tx.getMatchedEntry() != null ? tx.getMatchedEntry().getEntryId() : null)
                .matchedEntryNumber(tx.getMatchedEntry() != null ? tx.getMatchedEntry().getEntryNumber() : null)
                .build();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
