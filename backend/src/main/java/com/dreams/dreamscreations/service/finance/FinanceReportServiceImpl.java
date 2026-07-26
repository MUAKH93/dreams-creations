package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.GeneralLedgerLineDTO;
import com.dreams.dreamscreations.dto.finance.GeneralLedgerReportDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceLineDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceReportDTO;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.repository.finance.FinanceAccountRepository;
import com.dreams.dreamscreations.repository.finance.FinanceJournalLineRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceReportServiceImpl implements FinanceReportService {

    private final FinanceJournalLineRepository lineRepo;
    private final FinanceAccountRepository accountRepo;

    public FinanceReportServiceImpl(FinanceJournalLineRepository lineRepo,
                                    FinanceAccountRepository accountRepo) {
        this.lineRepo = lineRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceReportDTO getTrialBalance(boolean activeOnly, boolean includeZero) {
        List<FinanceJournalLineRepository.TrialBalanceProjection> rows =
                lineRepo.trialBalance(activeOnly ? 1 : 0, includeZero ? 1 : 0);

        BigDecimal grandDebit = BigDecimal.ZERO;
        BigDecimal grandCredit = BigDecimal.ZERO;
        List<TrialBalanceLineDTO> lines = new ArrayList<>();

        for (FinanceJournalLineRepository.TrialBalanceProjection row : rows) {
            BigDecimal debit = row.getTotalDebit() != null ? row.getTotalDebit() : BigDecimal.ZERO;
            BigDecimal credit = row.getTotalCredit() != null ? row.getTotalCredit() : BigDecimal.ZERO;
            BigDecimal balance = signedBalance(row.getAccountType(), debit, credit);
            lines.add(TrialBalanceLineDTO.builder()
                    .accountId(row.getAccountId())
                    .accountCode(row.getAccountCode())
                    .accountName(row.getAccountName())
                    .accountType(row.getAccountType())
                    .totalDebit(debit)
                    .totalCredit(credit)
                    .balance(balance)
                    .build());
            grandDebit = grandDebit.add(debit);
            grandCredit = grandCredit.add(credit);
        }

        return TrialBalanceReportDTO.builder()
                .lines(lines)
                .totalDebit(grandDebit)
                .totalCredit(grandCredit)
                .balanced(grandDebit.compareTo(grandCredit) == 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralLedgerReportDTO getGeneralLedger(Long accountId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new RuntimeException("From date and to date are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date cannot be after to date");
        }

        FinanceAccount account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        BigDecimal openingBalance = computeBalanceBefore(account, fromDate);
        List<FinanceJournalLineRepository.GeneralLedgerProjection> rows =
                lineRepo.generalLedger(accountId, fromDate, toDate);

        BigDecimal running = openingBalance;
        List<GeneralLedgerLineDTO> lines = new ArrayList<>();
        for (FinanceJournalLineRepository.GeneralLedgerProjection row : rows) {
            BigDecimal debit = row.getDebitAmount() != null ? row.getDebitAmount() : BigDecimal.ZERO;
            BigDecimal credit = row.getCreditAmount() != null ? row.getCreditAmount() : BigDecimal.ZERO;
            running = running.add(signedMovement(account.getAccountType(), debit, credit));
            lines.add(GeneralLedgerLineDTO.builder()
                    .entryDate(row.getEntryDate())
                    .entryNumber(row.getEntryNumber())
                    .entryMemo(row.getEntryMemo())
                    .lineMemo(row.getLineMemo())
                    .debitAmount(debit)
                    .creditAmount(credit)
                    .runningBalance(running)
                    .build());
        }

        return GeneralLedgerReportDTO.builder()
                .accountId(account.getAccountId())
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .fromDate(fromDate)
                .toDate(toDate)
                .openingBalance(openingBalance)
                .closingBalance(running)
                .lines(lines)
                .build();
    }

    private BigDecimal computeBalanceBefore(FinanceAccount account, LocalDate fromDate) {
        if (fromDate.equals(LocalDate.MIN)) {
            return BigDecimal.ZERO;
        }
        LocalDate dayBefore = fromDate.minusDays(1);
        List<FinanceJournalLineRepository.GeneralLedgerProjection> prior =
                lineRepo.generalLedger(account.getAccountId(), LocalDate.of(1900, 1, 1), dayBefore);
        BigDecimal balance = BigDecimal.ZERO;
        for (FinanceJournalLineRepository.GeneralLedgerProjection row : prior) {
            BigDecimal debit = row.getDebitAmount() != null ? row.getDebitAmount() : BigDecimal.ZERO;
            BigDecimal credit = row.getCreditAmount() != null ? row.getCreditAmount() : BigDecimal.ZERO;
            balance = balance.add(signedMovement(account.getAccountType(), debit, credit));
        }
        return balance;
    }

    private BigDecimal signedBalance(String accountType, BigDecimal debit, BigDecimal credit) {
        return signedMovement(accountType, debit, credit);
    }

    private BigDecimal signedMovement(String accountType, BigDecimal debit, BigDecimal credit) {
        if (isDebitNormal(accountType)) {
            return debit.subtract(credit);
        }
        return credit.subtract(debit);
    }

    private boolean isDebitNormal(String accountType) {
        return "ASSET".equalsIgnoreCase(accountType) || "EXPENSE".equalsIgnoreCase(accountType);
    }
}
