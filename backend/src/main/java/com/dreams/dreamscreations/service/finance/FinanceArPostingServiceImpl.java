package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Payment;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.entity.finance.FinanceJournalEntry;
import com.dreams.dreamscreations.entity.finance.FinanceJournalLine;
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
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceArPostingServiceImpl implements FinanceArPostingService {

    private static final String CODE_AR = "1100";
    private static final String CODE_CASH = "1000";
    private static final String CODE_REVENUE = "4000";
    private static final String CODE_DISCOUNT = "4100";

    private static final String ENTITY_BILL = "bill";
    private static final String ENTITY_BILL_VOID = "bill_void";
    private static final String ENTITY_PAYMENT = "payment";

    private final FinanceAccountRepository accountRepo;
    private final FinanceJournalEntryRepository entryRepo;
    private final FinancePostingLinkRepository linkRepo;

    public FinanceArPostingServiceImpl(FinanceAccountRepository accountRepo,
                                       FinanceJournalEntryRepository entryRepo,
                                       FinancePostingLinkRepository linkRepo) {
        this.accountRepo = accountRepo;
        this.entryRepo = entryRepo;
        this.linkRepo = linkRepo;
    }

    @Override
    @Transactional
    public void postBillSale(Bill bill) {
        if (bill == null || bill.getBillId() == null) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_BILL, bill.getBillId())) {
            return;
        }

        BigDecimal totalAmount = nz(bill.getTotalAmount());
        BigDecimal discount = nz(bill.getDiscount());
        BigDecimal finalAmount = nz(bill.getFinalAmount());
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        FinanceAccount ar = requireAccount(CODE_AR);
        FinanceAccount revenue = requireAccount(CODE_REVENUE);

        List<FinanceJournalLine> lines = new ArrayList<>();
        int order = 1;
        lines.add(line(ar, finalAmount, BigDecimal.ZERO, "AR — " + bill.getBillNumber(), order++));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            FinanceAccount discountAccount = requireAccount(CODE_DISCOUNT);
            lines.add(line(discountAccount, discount, BigDecimal.ZERO,
                    "Discount — " + bill.getBillNumber(), order++));
            lines.add(line(revenue, BigDecimal.ZERO, totalAmount,
                    "Sales — " + bill.getBillNumber(), order++));
        } else {
            lines.add(line(revenue, BigDecimal.ZERO, finalAmount,
                    "Sales — " + bill.getBillNumber(), order));
        }

        savePostedEntry(bill.getBillDate() != null ? bill.getBillDate().toLocalDate() : LocalDate.now(),
                "Bill " + bill.getBillNumber(),
                "bill", bill.getBillId(), ENTITY_BILL, bill.getBillId(), lines);
    }

    @Override
    @Transactional
    public void voidBillSale(Bill bill) {
        if (bill == null || bill.getBillId() == null) {
            return;
        }
        if (!linkRepo.existsByEntityTypeAndEntityId(ENTITY_BILL, bill.getBillId())) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_BILL_VOID, bill.getBillId())) {
            return;
        }

        BigDecimal totalAmount = nz(bill.getTotalAmount());
        BigDecimal discount = nz(bill.getDiscount());
        BigDecimal finalAmount = nz(bill.getFinalAmount());

        FinanceAccount ar = requireAccount(CODE_AR);
        FinanceAccount revenue = requireAccount(CODE_REVENUE);

        List<FinanceJournalLine> lines = new ArrayList<>();
        int order = 1;
        lines.add(line(ar, BigDecimal.ZERO, finalAmount, "Void AR — " + bill.getBillNumber(), order++));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            FinanceAccount discountAccount = requireAccount(CODE_DISCOUNT);
            lines.add(line(discountAccount, BigDecimal.ZERO, discount,
                    "Void discount — " + bill.getBillNumber(), order++));
            lines.add(line(revenue, totalAmount, BigDecimal.ZERO,
                    "Void sales — " + bill.getBillNumber(), order++));
        } else {
            lines.add(line(revenue, finalAmount, BigDecimal.ZERO,
                    "Void sales — " + bill.getBillNumber(), order));
        }

        savePostedEntry(LocalDate.now(),
                "Void bill " + bill.getBillNumber(),
                "bill_void", bill.getBillId(), ENTITY_BILL_VOID, bill.getBillId(), lines);
    }

    @Override
    @Transactional
    public void postPayment(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_PAYMENT, payment.getPaymentId())) {
            return;
        }

        BigDecimal amount = nz(payment.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Bill bill = payment.getBill();
        String billNumber = bill != null ? bill.getBillNumber() : "payment";

        FinanceAccount cash = requireAccount(CODE_CASH);
        FinanceAccount ar = requireAccount(CODE_AR);

        List<FinanceJournalLine> lines = List.of(
                line(cash, amount, BigDecimal.ZERO, "Payment — " + billNumber, 1),
                line(ar, BigDecimal.ZERO, amount, "Payment — " + billNumber, 2)
        );

        LocalDate entryDate = payment.getPaymentDate() != null
                ? payment.getPaymentDate().toLocalDate()
                : LocalDate.now();

        savePostedEntry(entryDate,
                "Payment for " + billNumber,
                "payment", payment.getPaymentId(),
                ENTITY_PAYMENT, payment.getPaymentId(), lines);
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
