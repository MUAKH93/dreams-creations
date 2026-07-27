package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinancePayablePaymentRequest;
import com.dreams.dreamscreations.dto.finance.CreateFinancePayableRequest;
import com.dreams.dreamscreations.dto.finance.FinancePayableDTO;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.entity.finance.FinancePayable;
import com.dreams.dreamscreations.entity.finance.FinancePayablePayment;
import com.dreams.dreamscreations.entity.finance.FinanceVendor;
import com.dreams.dreamscreations.repository.finance.FinanceAccountRepository;
import com.dreams.dreamscreations.repository.finance.FinancePayablePaymentRepository;
import com.dreams.dreamscreations.repository.finance.FinancePayableRepository;
import com.dreams.dreamscreations.repository.finance.FinanceVendorRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinancePayableServiceImpl implements FinancePayableService {

    private final FinancePayableRepository payableRepo;
    private final FinancePayablePaymentRepository paymentRepo;
    private final FinanceVendorRepository vendorRepo;
    private final FinanceAccountRepository accountRepo;
    private final FinanceApPostingService apPostingService;

    public FinancePayableServiceImpl(FinancePayableRepository payableRepo,
                                     FinancePayablePaymentRepository paymentRepo,
                                     FinanceVendorRepository vendorRepo,
                                     FinanceAccountRepository accountRepo,
                                     FinanceApPostingService apPostingService) {
        this.payableRepo = payableRepo;
        this.paymentRepo = paymentRepo;
        this.vendorRepo = vendorRepo;
        this.accountRepo = accountRepo;
        this.apPostingService = apPostingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancePayableDTO> getAll() {
        return payableRepo.findAllWithDetails().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinancePayableDTO getById(Long id) {
        return toDto(requirePayable(id));
    }

    @Override
    @Transactional
    public FinancePayableDTO create(CreateFinancePayableRequest request) {
        validateCreate(request);

        FinanceVendor vendor = vendorRepo.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + request.getVendorId()));
        if (!Boolean.TRUE.equals(vendor.getIsActive())) {
            throw new RuntimeException("Vendor is inactive");
        }

        String invoiceNumber = request.getInvoiceNumber().trim();
        if (payableRepo.existsByVendorAndInvoiceNumber(vendor, invoiceNumber)) {
            throw new RuntimeException("Invoice number already exists for this vendor");
        }

        FinanceAccount expenseAccount = accountRepo.findById(request.getExpenseAccountId())
                .orElseThrow(() -> new RuntimeException("Expense account not found: " + request.getExpenseAccountId()));
        if (!"EXPENSE".equalsIgnoreCase(expenseAccount.getAccountType())) {
            throw new RuntimeException("Selected account must be an expense account");
        }

        FinancePayable payable = FinancePayable.builder()
                .vendor(vendor)
                .invoiceNumber(invoiceNumber)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .amount(request.getAmount())
                .amountPaid(BigDecimal.ZERO)
                .expenseAccount(expenseAccount)
                .memo(trimOrNull(request.getMemo()))
                .status("unpaid")
                .build();

        FinancePayable saved = payableRepo.save(payable);
        apPostingService.postPayableInvoice(saved);

        return toDto(payableRepo.findByIdWithDetails(saved.getPayableId()).orElse(saved));
    }

    @Override
    @Transactional
    public FinancePayableDTO recordPayment(Long payableId, CreateFinancePayablePaymentRequest request) {
        FinancePayable payable = requirePayable(payableId);
        if ("paid".equals(payable.getStatus())) {
            throw new RuntimeException("Payable is already fully paid");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        BigDecimal balance = payable.getAmount().subtract(nz(payable.getAmountPaid()));
        if (amount.compareTo(balance) > 0) {
            throw new RuntimeException("Payment exceeds balance due (" + balance + ")");
        }

        LocalDate paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now();

        FinancePayablePayment payment = FinancePayablePayment.builder()
                .payable(payable)
                .paymentDate(paymentDate)
                .amount(amount)
                .paymentMethod(trimOrNull(request.getPaymentMethod()))
                .referenceNo(trimOrNull(request.getReferenceNo()))
                .build();

        FinancePayablePayment savedPayment = paymentRepo.save(payment);
        apPostingService.postPayablePayment(savedPayment);

        BigDecimal newPaid = nz(payable.getAmountPaid()).add(amount);
        payable.setAmountPaid(newPaid);
        if (newPaid.compareTo(payable.getAmount()) >= 0) {
            payable.setStatus("paid");
        } else {
            payable.setStatus("partial");
        }
        payableRepo.save(payable);

        return toDto(payableRepo.findByIdWithDetails(payableId).orElse(payable));
    }

    private FinancePayable requirePayable(Long id) {
        return payableRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Payable not found: " + id));
    }

    private void validateCreate(CreateFinancePayableRequest request) {
        if (request == null) {
            throw new RuntimeException("Request is required");
        }
        if (request.getVendorId() == null) {
            throw new RuntimeException("Vendor is required");
        }
        if (request.getInvoiceNumber() == null || request.getInvoiceNumber().isBlank()) {
            throw new RuntimeException("Invoice number is required");
        }
        if (request.getInvoiceDate() == null || request.getDueDate() == null) {
            throw new RuntimeException("Invoice date and due date are required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        if (request.getExpenseAccountId() == null) {
            throw new RuntimeException("Expense account is required");
        }
    }

    private FinancePayableDTO toDto(FinancePayable payable) {
        BigDecimal amountPaid = nz(payable.getAmountPaid());
        BigDecimal balance = payable.getAmount().subtract(amountPaid);
        FinanceAccount expense = payable.getExpenseAccount();
        FinanceVendor vendor = payable.getVendor();

        return FinancePayableDTO.builder()
                .payableId(payable.getPayableId())
                .vendorId(vendor != null ? vendor.getVendorId() : null)
                .vendorName(vendor != null ? vendor.getVendorName() : null)
                .invoiceNumber(payable.getInvoiceNumber())
                .invoiceDate(payable.getInvoiceDate())
                .dueDate(payable.getDueDate())
                .amount(payable.getAmount())
                .amountPaid(amountPaid)
                .balanceDue(balance)
                .expenseAccountId(expense != null ? expense.getAccountId() : null)
                .expenseAccountCode(expense != null ? expense.getAccountCode() : null)
                .expenseAccountName(expense != null ? expense.getAccountName() : null)
                .memo(payable.getMemo())
                .status(payable.getStatus())
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
