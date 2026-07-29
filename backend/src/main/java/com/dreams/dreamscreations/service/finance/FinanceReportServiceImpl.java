package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.ApAgingLineDTO;
import com.dreams.dreamscreations.dto.finance.ApAgingReportDTO;
import com.dreams.dreamscreations.dto.finance.ArAgingLineDTO;
import com.dreams.dreamscreations.dto.finance.ArAgingReportDTO;
import com.dreams.dreamscreations.dto.finance.ArReconciliationDTO;
import com.dreams.dreamscreations.dto.finance.BalanceSheetLineDTO;
import com.dreams.dreamscreations.dto.finance.BalanceSheetReportDTO;
import com.dreams.dreamscreations.dto.finance.GeneralLedgerLineDTO;
import com.dreams.dreamscreations.dto.finance.GeneralLedgerReportDTO;
import com.dreams.dreamscreations.dto.finance.InventoryValuationLineDTO;
import com.dreams.dreamscreations.dto.finance.InventoryValuationReportDTO;
import com.dreams.dreamscreations.dto.finance.ProfitLossLineDTO;
import com.dreams.dreamscreations.dto.finance.ProfitLossReportDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceLineDTO;
import com.dreams.dreamscreations.dto.finance.TrialBalanceReportDTO;
import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.CustomerBalance;
import com.dreams.dreamscreations.entity.Inventory;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.entity.finance.FinancePayable;
import com.dreams.dreamscreations.entity.finance.FinanceVendor;
import com.dreams.dreamscreations.repository.BillRepository;
import com.dreams.dreamscreations.repository.CustomerBalanceRepository;
import com.dreams.dreamscreations.repository.PaymentRepository;
import com.dreams.dreamscreations.repository.InventoryRepository;
import com.dreams.dreamscreations.repository.finance.FinanceAccountRepository;
import com.dreams.dreamscreations.repository.finance.FinanceJournalLineRepository;
import com.dreams.dreamscreations.repository.finance.FinancePayableRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceReportServiceImpl implements FinanceReportService {

    private final FinanceJournalLineRepository lineRepo;
    private final FinanceAccountRepository accountRepo;
    private final BillRepository billRepo;
    private final CustomerBalanceRepository balanceRepo;
    private final PaymentRepository paymentRepo;
    private final InventoryRepository inventoryRepo;
    private final FinancePayableRepository payableRepo;

    public FinanceReportServiceImpl(FinanceJournalLineRepository lineRepo,
                                    FinanceAccountRepository accountRepo,
                                    BillRepository billRepo,
                                    CustomerBalanceRepository balanceRepo,
                                    PaymentRepository paymentRepo,
                                    InventoryRepository inventoryRepo,
                                    FinancePayableRepository payableRepo) {
        this.lineRepo = lineRepo;
        this.accountRepo = accountRepo;
        this.billRepo = billRepo;
        this.balanceRepo = balanceRepo;
        this.paymentRepo = paymentRepo;
        this.inventoryRepo = inventoryRepo;
        this.payableRepo = payableRepo;
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

    @Override
    @Transactional(readOnly = true)
    public ArAgingReportDTO getArAging() {
        LocalDate today = LocalDate.now();
        Map<Long, MutableAgingLine> byCustomer = new LinkedHashMap<>();

        for (Bill bill : billRepo.findUnpaidWithCustomer()) {
            Customer customer = bill.getCustomer();
            if (customer == null) {
                continue;
            }
            Long customerId = customer.getCustomerId();
            MutableAgingLine row = byCustomer.computeIfAbsent(customerId, id -> {
                MutableAgingLine line = new MutableAgingLine();
                line.customerId = customerId;
                line.customerName = customerName(customer);
                line.phone = customer.getPhone();
                return line;
            });

            BigDecimal amount = nz(bill.getFinalAmount());
            BigDecimal paidOnBill = nz(paymentRepo.sumAmountByBill(bill.getBillId()));
            BigDecimal balanceDue = amount.subtract(paidOnBill);
            if (balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            long days = bill.getBillDate() != null
                    ? ChronoUnit.DAYS.between(bill.getBillDate().toLocalDate(), today)
                    : 0;

            if (days <= 30) {
                row.current = row.current.add(balanceDue);
            } else if (days <= 60) {
                row.days31to60 = row.days31to60.add(balanceDue);
            } else if (days <= 90) {
                row.days61to90 = row.days61to90.add(balanceDue);
            } else {
                row.over90 = row.over90.add(balanceDue);
            }
        }

        for (CustomerBalance balance : balanceRepo.findAllWithCustomer()) {
            if (balance.getCustomer() == null) {
                continue;
            }
            Long customerId = balance.getCustomer().getCustomerId();
            MutableAgingLine row = byCustomer.computeIfAbsent(customerId, id -> {
                Customer customer = balance.getCustomer();
                MutableAgingLine line = new MutableAgingLine();
                line.customerId = customerId;
                line.customerName = customerName(customer);
                line.phone = customer.getPhone();
                return line;
            });
            row.operationalBalance = nz(balance.getBalance());
        }

        BigDecimal totalCurrent = BigDecimal.ZERO;
        BigDecimal total31 = BigDecimal.ZERO;
        BigDecimal total61 = BigDecimal.ZERO;
        BigDecimal totalOver90 = BigDecimal.ZERO;
        List<ArAgingLineDTO> lines = new ArrayList<>();

        for (MutableAgingLine row : byCustomer.values()) {
            BigDecimal total = row.current.add(row.days31to60).add(row.days61to90).add(row.over90);
            if (total.compareTo(BigDecimal.ZERO) == 0 && row.operationalBalance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            lines.add(ArAgingLineDTO.builder()
                    .customerId(row.customerId)
                    .customerName(row.customerName)
                    .phone(row.phone)
                    .current(row.current)
                    .days31to60(row.days31to60)
                    .days61to90(row.days61to90)
                    .over90(row.over90)
                    .totalOutstanding(total)
                    .operationalBalance(row.operationalBalance)
                    .build());
            totalCurrent = totalCurrent.add(row.current);
            total31 = total31.add(row.days31to60);
            total61 = total61.add(row.days61to90);
            totalOver90 = totalOver90.add(row.over90);
        }

        return ArAgingReportDTO.builder()
                .lines(lines)
                .totalCurrent(totalCurrent)
                .totalDays31to60(total31)
                .totalDays61to90(total61)
                .totalOver90(totalOver90)
                .grandTotal(totalCurrent.add(total31).add(total61).add(totalOver90))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApAgingReportDTO getApAging() {
        LocalDate today = LocalDate.now();
        Map<Long, MutableApAgingLine> byVendor = new LinkedHashMap<>();

        for (FinancePayable payable : payableRepo.findOpenWithVendor()) {
            FinanceVendor vendor = payable.getVendor();
            if (vendor == null) {
                continue;
            }
            Long vendorId = vendor.getVendorId();
            MutableApAgingLine row = byVendor.computeIfAbsent(vendorId, id -> {
                MutableApAgingLine line = new MutableApAgingLine();
                line.vendorId = vendorId;
                line.vendorName = vendor.getVendorName();
                line.phone = vendor.getPhone();
                return line;
            });

            BigDecimal balance = nz(payable.getAmount()).subtract(nz(payable.getAmountPaid()));
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            long days = payable.getInvoiceDate() != null
                    ? ChronoUnit.DAYS.between(payable.getInvoiceDate(), today)
                    : 0;

            if (days <= 30) {
                row.current = row.current.add(balance);
            } else if (days <= 60) {
                row.days31to60 = row.days31to60.add(balance);
            } else if (days <= 90) {
                row.days61to90 = row.days61to90.add(balance);
            } else {
                row.over90 = row.over90.add(balance);
            }
        }

        BigDecimal totalCurrent = BigDecimal.ZERO;
        BigDecimal total31 = BigDecimal.ZERO;
        BigDecimal total61 = BigDecimal.ZERO;
        BigDecimal totalOver90 = BigDecimal.ZERO;
        List<ApAgingLineDTO> lines = new ArrayList<>();

        for (MutableApAgingLine row : byVendor.values()) {
            BigDecimal total = row.current.add(row.days31to60).add(row.days61to90).add(row.over90);
            if (total.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            lines.add(ApAgingLineDTO.builder()
                    .vendorId(row.vendorId)
                    .vendorName(row.vendorName)
                    .phone(row.phone)
                    .current(row.current)
                    .days31to60(row.days31to60)
                    .days61to90(row.days61to90)
                    .over90(row.over90)
                    .totalOutstanding(total)
                    .build());
            totalCurrent = totalCurrent.add(row.current);
            total31 = total31.add(row.days31to60);
            total61 = total61.add(row.days61to90);
            totalOver90 = totalOver90.add(row.over90);
        }

        BigDecimal grandTotal = totalCurrent.add(total31).add(total61).add(totalOver90);

        TrialBalanceReportDTO trialBalance = getTrialBalance(true, false);
        BigDecimal ledgerAp = trialBalance.getLines().stream()
                .filter(line -> CODE_AP.equals(line.getAccountCode()))
                .map(TrialBalanceLineDTO::getBalance)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal difference = ledgerAp.subtract(grandTotal);
        boolean reconciled = difference.abs().compareTo(new BigDecimal("0.01")) <= 0;

        return ApAgingReportDTO.builder()
                .lines(lines)
                .totalCurrent(totalCurrent)
                .totalDays31to60(total31)
                .totalDays61to90(total61)
                .totalOver90(totalOver90)
                .grandTotal(grandTotal)
                .ledgerApBalance(ledgerAp)
                .difference(difference)
                .reconciled(reconciled)
                .message(reconciled
                        ? "AP ledger matches open payables."
                        : "Difference detected — review payable journals or manual AP entries.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArReconciliationDTO getArReconciliation() {
        TrialBalanceReportDTO trialBalance = getTrialBalance(true, false);
        BigDecimal ledgerAr = trialBalance.getLines().stream()
                .filter(line -> "1100".equals(line.getAccountCode()))
                .map(TrialBalanceLineDTO::getBalance)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal operationalTotal = balanceRepo.findAll().stream()
                .map(CustomerBalance::getBalance)
                .map(this::nz)
                .filter(balance -> balance.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal difference = ledgerAr.subtract(operationalTotal);
        boolean reconciled = difference.abs().compareTo(new BigDecimal("0.01")) <= 0;

        return ArReconciliationDTO.builder()
                .ledgerArBalance(ledgerAr)
                .operationalBalanceTotal(operationalTotal)
                .difference(difference)
                .reconciled(reconciled)
                .message(reconciled
                        ? "AR ledger matches operational customer balances."
                        : "Difference detected — review auto-posted journals or manual AR entries.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryValuationReportDTO getInventoryValuation() {
        TrialBalanceReportDTO trialBalance = getTrialBalance(true, false);
        BigDecimal ledgerInventory = trialBalance.getLines().stream()
                .filter(line -> CODE_INVENTORY.equals(line.getAccountCode()))
                .map(TrialBalanceLineDTO::getBalance)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        List<InventoryValuationLineDTO> lines = new ArrayList<>();
        BigDecimal operationalValue = BigDecimal.ZERO;
        int totalUnits = 0;
        int linesMissingCost = 0;

        for (Inventory inv : inventoryRepo.findAllWithDetails()) {
            Suit suit = inv.getSuit();
            if (suit == null || suit.getDesign() == null) {
                continue;
            }
            int qty = inv.getQuantity() != null ? inv.getQuantity() : 0;
            if (qty <= 0) {
                continue;
            }

            BigDecimal unitCost = nz(suit.getDesign().getProductionCost());
            BigDecimal lineValue = unitCost.multiply(BigDecimal.valueOf(qty));
            if (unitCost.compareTo(BigDecimal.ZERO) <= 0) {
                linesMissingCost++;
            }

            lines.add(InventoryValuationLineDTO.builder()
                    .suitId(suit.getSuitId())
                    .designCode(suit.getDesign().getDesignCode())
                    .designName(suit.getDesign().getName())
                    .sizeValue(suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD")
                    .color(suit.getColor())
                    .quantity(qty)
                    .unitCost(unitCost)
                    .lineValue(lineValue)
                    .build());

            totalUnits += qty;
            operationalValue = operationalValue.add(lineValue);
        }

        BigDecimal difference = ledgerInventory.subtract(operationalValue);
        boolean reconciled = difference.abs().compareTo(new BigDecimal("0.01")) <= 0;

        String message;
        if (linesMissingCost > 0) {
            message = linesMissingCost + " SKU line(s) have no production cost on the design — set production cost for accurate valuation.";
        } else if (reconciled) {
            message = "Inventory ledger matches operational stock at standard cost.";
        } else {
            message = "Difference detected — review auto-posted COGS/receipt journals or manual inventory entries.";
        }

        return InventoryValuationReportDTO.builder()
                .ledgerInventoryBalance(ledgerInventory)
                .operationalStockValue(operationalValue)
                .difference(difference)
                .reconciled(reconciled && linesMissingCost == 0)
                .totalUnits(totalUnits)
                .linesMissingCost(linesMissingCost)
                .message(message)
                .lines(lines)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfitLossReportDTO getProfitLoss(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<ProfitLossLineDTO> incomeLines = new ArrayList<>();
        List<ProfitLossLineDTO> expenseLines = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (FinanceJournalLineRepository.TrialBalanceProjection row : lineRepo.profitLossActivity(fromDate, toDate)) {
            BigDecimal debit = nz(row.getTotalDebit());
            BigDecimal credit = nz(row.getTotalCredit());
            BigDecimal amount = periodAmount(row.getAccountType(), debit, credit);
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            ProfitLossLineDTO line = ProfitLossLineDTO.builder()
                    .accountId(row.getAccountId())
                    .accountCode(row.getAccountCode())
                    .accountName(row.getAccountName())
                    .accountType(row.getAccountType())
                    .amount(amount)
                    .build();

            if ("INCOME".equalsIgnoreCase(row.getAccountType())) {
                incomeLines.add(line);
                totalIncome = totalIncome.add(amount);
            } else if ("EXPENSE".equalsIgnoreCase(row.getAccountType())) {
                expenseLines.add(line);
                totalExpenses = totalExpenses.add(amount);
            }
        }

        return ProfitLossReportDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .incomeLines(incomeLines)
                .expenseLines(expenseLines)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netIncome(totalIncome.subtract(totalExpenses))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetReportDTO getBalanceSheet(LocalDate asOfDate) {
        if (asOfDate == null) {
            throw new RuntimeException("As-of date is required");
        }

        List<BalanceSheetLineDTO> assetLines = new ArrayList<>();
        List<BalanceSheetLineDTO> liabilityLines = new ArrayList<>();
        List<BalanceSheetLineDTO> equityLines = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (FinanceJournalLineRepository.TrialBalanceProjection row : lineRepo.balanceSheetAsOf(asOfDate)) {
            BigDecimal debit = nz(row.getTotalDebit());
            BigDecimal credit = nz(row.getTotalCredit());
            BigDecimal balance = signedBalance(row.getAccountType(), debit, credit);
            if (balance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BalanceSheetLineDTO line = BalanceSheetLineDTO.builder()
                    .accountId(row.getAccountId())
                    .accountCode(row.getAccountCode())
                    .accountName(row.getAccountName())
                    .section(row.getAccountType())
                    .balance(balance)
                    .build();

            switch (row.getAccountType().toUpperCase()) {
                case "ASSET" -> {
                    assetLines.add(line);
                    totalAssets = totalAssets.add(balance);
                }
                case "LIABILITY" -> {
                    liabilityLines.add(line);
                    totalLiabilities = totalLiabilities.add(balance);
                }
                case "EQUITY" -> {
                    equityLines.add(line);
                    totalEquity = totalEquity.add(balance);
                }
                default -> { }
            }
        }

        BigDecimal liabilitiesPlusEquity = totalLiabilities.add(totalEquity);
        BigDecimal difference = totalAssets.subtract(liabilitiesPlusEquity);
        boolean balanced = difference.abs().compareTo(new BigDecimal("0.01")) <= 0;

        String message = balanced
                ? "Balance sheet balances — assets equal liabilities plus equity."
                : "Out of balance — current-period profit may not yet be closed to retained earnings.";

        return BalanceSheetReportDTO.builder()
                .asOfDate(asOfDate)
                .assetLines(assetLines)
                .liabilityLines(liabilityLines)
                .equityLines(equityLines)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .difference(difference)
                .balanced(balanced)
                .message(message)
                .build();
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new RuntimeException("From date and to date are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date cannot be after to date");
        }
    }

    private BigDecimal periodAmount(String accountType, BigDecimal debit, BigDecimal credit) {
        if ("INCOME".equalsIgnoreCase(accountType)) {
            return credit.subtract(debit);
        }
        if ("EXPENSE".equalsIgnoreCase(accountType)) {
            return debit.subtract(credit);
        }
        return signedBalance(accountType, debit, credit).abs();
    }

    private static final String CODE_INVENTORY = "1200";
    private static final String CODE_AP = "2000";

    private String customerName(Customer customer) {
        String name = ((customer.getFirstName() != null ? customer.getFirstName() : "")
                + " " + (customer.getLastName() != null ? customer.getLastName() : "")).trim();
        return name.isEmpty() ? "Customer #" + customer.getCustomerId() : name;
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static class MutableAgingLine {
        Long customerId;
        String customerName;
        String phone;
        BigDecimal current = BigDecimal.ZERO;
        BigDecimal days31to60 = BigDecimal.ZERO;
        BigDecimal days61to90 = BigDecimal.ZERO;
        BigDecimal over90 = BigDecimal.ZERO;
        BigDecimal operationalBalance = BigDecimal.ZERO;
    }

    private static class MutableApAgingLine {
        Long vendorId;
        String vendorName;
        String phone;
        BigDecimal current = BigDecimal.ZERO;
        BigDecimal days31to60 = BigDecimal.ZERO;
        BigDecimal days61to90 = BigDecimal.ZERO;
        BigDecimal over90 = BigDecimal.ZERO;
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
