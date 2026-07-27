package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.entity.finance.FinanceJournalEntry;
import com.dreams.dreamscreations.entity.finance.FinanceJournalLine;
import com.dreams.dreamscreations.entity.finance.FinancePayable;
import com.dreams.dreamscreations.entity.finance.FinancePayablePayment;
import com.dreams.dreamscreations.entity.finance.FinancePostingLink;
import com.dreams.dreamscreations.repository.finance.FinanceAccountRepository;
import com.dreams.dreamscreations.repository.finance.FinanceJournalEntryRepository;
import com.dreams.dreamscreations.repository.finance.FinancePostingLinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceApPostingServiceImpl implements FinanceApPostingService {

    private static final String CODE_AP = "2000";
    private static final String CODE_CASH = "1000";

    private static final String ENTITY_PAYABLE = "finance_payable";
    private static final String ENTITY_PAYABLE_PAYMENT = "finance_payable_payment";

    private final FinanceAccountRepository accountRepo;
    private final FinanceJournalEntryRepository entryRepo;
    private final FinancePostingLinkRepository linkRepo;

    public FinanceApPostingServiceImpl(FinanceAccountRepository accountRepo,
                                       FinanceJournalEntryRepository entryRepo,
                                       FinancePostingLinkRepository linkRepo) {
        this.accountRepo = accountRepo;
        this.entryRepo = entryRepo;
        this.linkRepo = linkRepo;
    }

    @Override
    @Transactional
    public void postPayableInvoice(FinancePayable payable) {
        if (payable == null || payable.getPayableId() == null) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_PAYABLE, payable.getPayableId())) {
            return;
        }

        BigDecimal amount = nz(payable.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        FinanceAccount expense = payable.getExpenseAccount();
        if (expense == null) {
            throw new RuntimeException("Expense account is required for payable posting");
        }
        FinanceAccount ap = requireAccount(CODE_AP);

        String ref = payable.getInvoiceNumber();
        List<FinanceJournalLine> lines = List.of(
                line(expense, amount, BigDecimal.ZERO, "Expense — " + ref, 1),
                line(ap, BigDecimal.ZERO, amount, "AP — " + ref, 2)
        );

        savePostedEntry(payable.getInvoiceDate(),
                "Vendor invoice " + ref,
                "finance_payable", payable.getPayableId(),
                ENTITY_PAYABLE, payable.getPayableId(), lines);
    }

    @Override
    @Transactional
    public void postPayablePayment(FinancePayablePayment payment) {
        if (payment == null || payment.getPaymentId() == null) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_PAYABLE_PAYMENT, payment.getPaymentId())) {
            return;
        }

        BigDecimal amount = nz(payment.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        FinancePayable payable = payment.getPayable();
        String ref = payable != null ? payable.getInvoiceNumber() : "payment";

        FinanceAccount ap = requireAccount(CODE_AP);
        FinanceAccount cash = requireAccount(CODE_CASH);

        List<FinanceJournalLine> lines = List.of(
                line(ap, amount, BigDecimal.ZERO, "AP payment — " + ref, 1),
                line(cash, BigDecimal.ZERO, amount, "Cash — " + ref, 2)
        );

        LocalDate entryDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();

        savePostedEntry(entryDate,
                "Payment for " + ref,
                "finance_payable_payment", payment.getPaymentId(),
                ENTITY_PAYABLE_PAYMENT, payment.getPaymentId(), lines);
    }

    private void savePostedEntry(LocalDate entryDate,
                                 String memo,
                                 String sourceType,
                                 Long sourceId,
                                 String linkEntityType,
                                 Long linkEntityId,
                                 List<FinanceJournalLine> lines) {
        FinanceJournalEntry entry = FinanceJournalEntry.builder()
                .entryNumber(generateEntryNumber(entryDate))
                .entryDate(entryDate)
                .memo(memo)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .status("posted")
                .postedAt(LocalDateTime.now())
                .build();

        for (FinanceJournalLine line : lines) {
            line.setEntry(entry);
        }
        entry.setLines(lines);

        FinanceJournalEntry saved = entryRepo.save(entry);
        linkRepo.save(FinancePostingLink.builder()
                .entry(saved)
                .entityType(linkEntityType)
                .entityId(linkEntityId)
                .build());
    }

    private FinanceJournalLine line(FinanceAccount account,
                                    BigDecimal debit,
                                    BigDecimal credit,
                                    String memo,
                                    int order) {
        return FinanceJournalLine.builder()
                .account(account)
                .debitAmount(debit)
                .creditAmount(credit)
                .lineMemo(memo)
                .lineOrder(order)
                .build();
    }

    private FinanceAccount requireAccount(String code) {
        return accountRepo.findByAccountCode(code)
                .orElseThrow(() -> new RuntimeException(
                        "Finance account " + code + " not found — run add-finance-module.sql"));
    }

    private String generateEntryNumber(LocalDate entryDate) {
        String prefix = "JE-" + entryDate.getYear() + "-";
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
}
