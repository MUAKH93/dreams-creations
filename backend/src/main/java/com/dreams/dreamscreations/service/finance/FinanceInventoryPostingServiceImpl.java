package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.entity.finance.FinanceJournalEntry;
import com.dreams.dreamscreations.entity.finance.FinanceJournalLine;
import com.dreams.dreamscreations.entity.finance.FinancePostingLink;
import com.dreams.dreamscreations.repository.BillItemRepository;
import com.dreams.dreamscreations.repository.BillRepository;
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
public class FinanceInventoryPostingServiceImpl implements FinanceInventoryPostingService {

    private static final String CODE_INVENTORY = "1200";
    private static final String CODE_WIP = "1300";
    private static final String CODE_COGS = "5000";

    private static final String ENTITY_BILL_COGS = "bill_cogs";
    private static final String ENTITY_BILL_COGS_VOID = "bill_cogs_void";
    private static final String ENTITY_PRODUCTION_RECEIPT = "production_receipt";

    private final FinanceAccountRepository accountRepo;
    private final FinanceJournalEntryRepository entryRepo;
    private final FinancePostingLinkRepository linkRepo;
    private final BillRepository billRepo;
    private final BillItemRepository billItemRepo;

    public FinanceInventoryPostingServiceImpl(FinanceAccountRepository accountRepo,
                                              FinanceJournalEntryRepository entryRepo,
                                              FinancePostingLinkRepository linkRepo,
                                              BillRepository billRepo,
                                              BillItemRepository billItemRepo) {
        this.accountRepo = accountRepo;
        this.entryRepo = entryRepo;
        this.linkRepo = linkRepo;
        this.billRepo = billRepo;
        this.billItemRepo = billItemRepo;
    }

    @Override
    @Transactional
    public void postBillCogs(Bill bill) {
        if (bill == null || bill.getBillId() == null) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_BILL_COGS, bill.getBillId())) {
            return;
        }

        Bill detailed = billRepo.findByIdWithDetails(bill.getBillId()).orElse(bill);
        BigDecimal totalCogs = computeBillCogs(detailed);
        if (totalCogs.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        FinanceAccount cogs = requireAccount(CODE_COGS);
        FinanceAccount inventory = requireAccount(CODE_INVENTORY);

        List<FinanceJournalLine> lines = List.of(
                line(cogs, totalCogs, BigDecimal.ZERO, "COGS — " + detailed.getBillNumber(), 1),
                line(inventory, BigDecimal.ZERO, totalCogs, "Inventory out — " + detailed.getBillNumber(), 2)
        );

        LocalDate entryDate = detailed.getBillDate() != null
                ? detailed.getBillDate().toLocalDate()
                : LocalDate.now();

        savePostedEntry(entryDate,
                "COGS for bill " + detailed.getBillNumber(),
                "bill_cogs", detailed.getBillId(),
                ENTITY_BILL_COGS, detailed.getBillId(), lines);
    }

    @Override
    @Transactional
    public void voidBillCogs(Bill bill) {
        if (bill == null || bill.getBillId() == null) {
            return;
        }
        if (!linkRepo.existsByEntityTypeAndEntityId(ENTITY_BILL_COGS, bill.getBillId())) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_BILL_COGS_VOID, bill.getBillId())) {
            return;
        }

        Bill detailed = billRepo.findByIdWithDetails(bill.getBillId()).orElse(bill);
        BigDecimal totalCogs = computeBillCogs(detailed);
        if (totalCogs.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        FinanceAccount cogs = requireAccount(CODE_COGS);
        FinanceAccount inventory = requireAccount(CODE_INVENTORY);

        List<FinanceJournalLine> lines = List.of(
                line(cogs, BigDecimal.ZERO, totalCogs, "Void COGS — " + detailed.getBillNumber(), 1),
                line(inventory, totalCogs, BigDecimal.ZERO, "Restore inventory — " + detailed.getBillNumber(), 2)
        );

        savePostedEntry(LocalDate.now(),
                "Void COGS for bill " + detailed.getBillNumber(),
                "bill_cogs_void", detailed.getBillId(),
                ENTITY_BILL_COGS_VOID, detailed.getBillId(), lines);
    }

    @Override
    @Transactional
    public void postProductionReceipt(ModuleAssignment assignment) {
        if (assignment == null || assignment.getAssignmentId() == null) {
            return;
        }
        if (linkRepo.existsByEntityTypeAndEntityId(ENTITY_PRODUCTION_RECEIPT, assignment.getAssignmentId())) {
            return;
        }

        ProductionBatch batch = assignment.getBatch();
        if (batch == null || batch.getSuit() == null || batch.getSuit().getDesign() == null) {
            return;
        }

        BigDecimal totalValue = computeProductionReceiptValue(assignment, batch);
        if (totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        FinanceAccount inventory = requireAccount(CODE_INVENTORY);
        FinanceAccount wip = requireAccount(CODE_WIP);

        String batchNumber = batch.getBatchNumber() != null ? batch.getBatchNumber() : "batch";
        List<FinanceJournalLine> lines = List.of(
                line(inventory, totalValue, BigDecimal.ZERO, "FG receipt — " + batchNumber, 1),
                line(wip, BigDecimal.ZERO, totalValue, "WIP clearance — " + batchNumber, 2)
        );

        LocalDate entryDate = assignment.getCompletionDate() != null
                ? assignment.getCompletionDate().toLocalDate()
                : LocalDate.now();

        savePostedEntry(entryDate,
                "Production receipt " + batchNumber,
                "production_receipt", assignment.getAssignmentId(),
                ENTITY_PRODUCTION_RECEIPT, assignment.getAssignmentId(), lines);
    }

    private BigDecimal computeBillCogs(Bill bill) {
        List<BillItem> items = bill.getItems();
        if (items == null || items.isEmpty()) {
            items = billItemRepo.findByBill(bill);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BillItem item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            Product product = item.getProduct();
            if (product == null || product.getSuit() == null || product.getSuit().getDesign() == null) {
                continue;
            }
            BigDecimal unitCost = nz(product.getSuit().getDesign().getProductionCost());
            if (unitCost.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            total = total.add(unitCost.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private BigDecimal computeProductionReceiptValue(ModuleAssignment assignment, ProductionBatch batch) {
        Design design = batch.getSuit().getDesign();
        BigDecimal unitCost = nz(design.getProductionCost());
        if (unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        int totalQty = 0;
        if (assignment.getSkuLines() != null && !assignment.getSkuLines().isEmpty()) {
            for (ModuleAssignmentSkuLine line : assignment.getSkuLines()) {
                if (line.getQuantityReturnedOk() != null && line.getQuantityReturnedOk() > 0) {
                    totalQty += line.getQuantityReturnedOk();
                }
            }
        } else if (assignment.getQuantityReturnedOk() != null && assignment.getQuantityReturnedOk() > 0) {
            totalQty = assignment.getQuantityReturnedOk();
        }

        if (totalQty <= 0) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(BigDecimal.valueOf(totalQty));
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
        entry.setLines(new ArrayList<>(lines));

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
