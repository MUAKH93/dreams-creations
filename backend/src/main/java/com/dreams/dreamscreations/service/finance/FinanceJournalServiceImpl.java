package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.*;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.entity.finance.FinanceJournalEntry;
import com.dreams.dreamscreations.entity.finance.FinanceJournalLine;
import com.dreams.dreamscreations.repository.finance.FinanceAccountRepository;
import com.dreams.dreamscreations.repository.finance.FinanceJournalEntryRepository;
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
public class FinanceJournalServiceImpl implements FinanceJournalService {

    private final FinanceJournalEntryRepository entryRepo;
    private final FinanceAccountRepository accountRepo;

    public FinanceJournalServiceImpl(FinanceJournalEntryRepository entryRepo,
                                     FinanceAccountRepository accountRepo) {
        this.entryRepo = entryRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceJournalEntryDTO> getAll() {
        return entryRepo.findAllWithLines().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceJournalEntryDTO getById(Long id) {
        return entryRepo.findByIdWithLines(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + id));
    }

    @Override
    @Transactional
    public FinanceJournalEntryDTO createManualEntry(CreateFinanceJournalRequest request, Long userId) {
        if (request.getEntryDate() == null) {
            throw new RuntimeException("Entry date is required");
        }
        if (request.getLines() == null || request.getLines().size() < 2) {
            throw new RuntimeException("At least two journal lines are required");
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<FinanceJournalLine> lines = new ArrayList<>();
        int order = 1;

        FinanceJournalEntry entry = FinanceJournalEntry.builder()
                .entryNumber(generateEntryNumber(request.getEntryDate()))
                .entryDate(request.getEntryDate())
                .memo(request.getMemo())
                .sourceType("manual")
                .status("posted")
                .createdBy(userId)
                .postedAt(LocalDateTime.now())
                .build();

        for (CreateFinanceJournalLineRequest lineReq : request.getLines()) {
            if (lineReq.getAccountId() == null) {
                throw new RuntimeException("Each line needs an account");
            }
            BigDecimal debit = nz(lineReq.getDebitAmount());
            BigDecimal credit = nz(lineReq.getCreditAmount());
            if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
                throw new RuntimeException("Each line needs a debit or credit amount");
            }
            if (debit.compareTo(BigDecimal.ZERO) > 0 && credit.compareTo(BigDecimal.ZERO) > 0) {
                throw new RuntimeException("A line cannot have both debit and credit amounts");
            }

            FinanceAccount account = accountRepo.findById(lineReq.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found: " + lineReq.getAccountId()));
            if (!Boolean.TRUE.equals(account.getIsActive())) {
                throw new RuntimeException("Account is inactive: " + account.getAccountCode());
            }

            FinanceJournalLine line = FinanceJournalLine.builder()
                    .entry(entry)
                    .account(account)
                    .debitAmount(debit)
                    .creditAmount(credit)
                    .lineMemo(lineReq.getLineMemo())
                    .lineOrder(order++)
                    .build();
            lines.add(line);
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new RuntimeException("Journal entry is not balanced: debits "
                    + totalDebit + " != credits " + totalCredit);
        }

        entry.setLines(lines);
        FinanceJournalEntry saved = entryRepo.save(entry);
        return entryRepo.findByIdWithLines(saved.getEntryId())
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Failed to load saved journal entry"));
    }

    private String generateEntryNumber(LocalDate entryDate) {
        int year = entryDate.getYear();
        String prefix = "JE-" + year + "-";
        return entryRepo.findTopByEntryNumberStartingWithOrderByEntryNumberDesc(prefix)
                .map(FinanceJournalEntry::getEntryNumber)
                .map(latest -> {
                    String suffix = latest.substring(prefix.length());
                    int next = Integer.parseInt(suffix) + 1;
                    return prefix + String.format("%03d", next);
                })
                .orElse(prefix + "001");
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private FinanceJournalEntryDTO toDto(FinanceJournalEntry entry) {
        List<FinanceJournalLineDTO> lineDtos = entry.getLines() != null
                ? entry.getLines().stream().map(this::toLineDto).toList()
                : List.of();
        BigDecimal totalDebit = lineDtos.stream()
                .map(FinanceJournalLineDTO::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lineDtos.stream()
                .map(FinanceJournalLineDTO::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinanceJournalEntryDTO.builder()
                .entryId(entry.getEntryId())
                .entryNumber(entry.getEntryNumber())
                .entryDate(entry.getEntryDate())
                .memo(entry.getMemo())
                .sourceType(entry.getSourceType())
                .status(entry.getStatus())
                .postedAt(entry.getPostedAt())
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .lines(lineDtos)
                .build();
    }

    private FinanceJournalLineDTO toLineDto(FinanceJournalLine line) {
        FinanceAccount account = line.getAccount();
        return FinanceJournalLineDTO.builder()
                .lineId(line.getLineId())
                .accountId(account.getAccountId())
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .debitAmount(line.getDebitAmount())
                .creditAmount(line.getCreditAmount())
                .lineMemo(line.getLineMemo())
                .lineOrder(line.getLineOrder())
                .build();
    }
}
