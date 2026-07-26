package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.QuotationListDTO;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.ActivityLogService;
import com.dreams.dreamscreations.service.BillService;
import com.dreams.dreamscreations.service.QuotationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepo;
    private final CustomerRepository customerRepo;
    private final DesignRepository designRepo;
    private final SizeRepository sizeRepo;
    private final SuitRepository suitRepo;
    private final ProductRepository productRepo;
    private final BillService billService;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;

    public QuotationServiceImpl(QuotationRepository quotationRepo,
                                  CustomerRepository customerRepo,
                                  DesignRepository designRepo,
                                  SizeRepository sizeRepo,
                                  SuitRepository suitRepo,
                                  ProductRepository productRepo,
                                  BillService billService,
                                  CurrentUserService currentUserService,
                                  ActivityLogService activityLogService) {
        this.quotationRepo = quotationRepo;
        this.customerRepo = customerRepo;
        this.designRepo = designRepo;
        this.sizeRepo = sizeRepo;
        this.suitRepo = suitRepo;
        this.productRepo = productRepo;
        this.billService = billService;
        this.currentUserService = currentUserService;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional
    public Quotation create(Quotation quotation) {
        User user = currentUserService.getCurrentUser();
        boolean isCustomer = isCustomerRole(user);

        Customer customer;
        if (isCustomer) {
            customer = customerRepo.findById(currentUserService.requireCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            quotation.setCustomer(customer);
            quotation.setStatus("draft");
        } else {
            if (quotation.getCustomer() == null || quotation.getCustomer().getCustomerId() == null) {
                throw new RuntimeException("Customer is required");
            }
            customer = customerRepo.findById(quotation.getCustomer().getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            quotation.setCustomer(customer);
            if (quotation.getStatus() == null || quotation.getStatus().isBlank()) {
                quotation.setStatus("draft");
            }
        }

        if (quotation.getQuotationNumber() == null || quotation.getQuotationNumber().isBlank()) {
            quotation.setQuotationNumber(generateNextQuotationNumber());
        }
        if (quotationRepo.findByQuotationNumber(quotation.getQuotationNumber()).isPresent()) {
            throw new RuntimeException("Quotation number already exists: " + quotation.getQuotationNumber());
        }

        quotation.setCreatedBy(user);
        resolveItems(quotation);
        computeTotals(quotation, customer);

        Quotation saved = quotationRepo.save(quotation);
        activityLogService.log(user, "QUOTATION_CREATED", "QUOTATION", saved.getQuotationId(),
                "Created quotation " + saved.getQuotationNumber());
        return quotationRepo.findByIdWithDetails(saved.getQuotationId()).orElse(saved);
    }

    @Override
    @Transactional
    public Quotation update(Long id, Quotation updated) {
        Quotation existing = getById(id);
        if (!"draft".equalsIgnoreCase(existing.getStatus())) {
            throw new RuntimeException("Only draft quotations can be edited");
        }
        ensureCanModify(existing);

        existing.setNotes(updated.getNotes());
        if (updated.getDiscount() != null) {
            existing.setDiscount(updated.getDiscount());
        }
        if (updated.getItems() != null) {
            if (existing.getItems() == null) {
                existing.setItems(new ArrayList<>());
            } else {
                existing.getItems().clear();
            }
            for (QuotationItem src : updated.getItems()) {
                existing.getItems().add(QuotationItem.builder()
                        .design(src.getDesign())
                        .size(src.getSize())
                        .color(src.getColor())
                        .quantity(src.getQuantity())
                        .unitPrice(src.getUnitPrice())
                        .notes(src.getNotes())
                        .build());
            }
            resolveItems(existing);
        }
        computeTotals(existing, existing.getCustomer());

        Quotation saved = quotationRepo.save(existing);
        activityLogService.log(currentUserService.getCurrentUser(), "QUOTATION_UPDATED", "QUOTATION", id,
                "Updated quotation " + saved.getQuotationNumber());
        return quotationRepo.findByIdWithDetails(saved.getQuotationId()).orElse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationListDTO> getAllSummaries() {
        return quotationRepo.findAllWithDetails().stream()
                .map(this::toListDto)
                .toList();
    }

    private QuotationListDTO toListDto(Quotation q) {
        String name = "";
        Long customerId = null;
        if (q.getCustomer() != null) {
            customerId = q.getCustomer().getCustomerId();
            name = (q.getCustomer().getFirstName() + " "
                    + (q.getCustomer().getLastName() != null ? q.getCustomer().getLastName() : "")).trim();
        }
        int itemCount = q.getItems() != null ? q.getItems().size() : 0;
        return QuotationListDTO.builder()
                .quotationId(q.getQuotationId())
                .quotationNumber(q.getQuotationNumber())
                .customerId(customerId)
                .customerName(name)
                .status(q.getStatus())
                .totalAmount(q.getTotalAmount())
                .discount(q.getDiscount())
                .finalAmount(q.getFinalAmount())
                .createdAt(q.getCreatedAt())
                .itemCount(itemCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Quotation getById(Long id) {
        Quotation quotation = quotationRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found: " + id));
        User user = currentUserService.getCurrentUser();
        if (isCustomerRole(user)) {
            Long customerId = currentUserService.requireCustomerId();
            if (!quotation.getCustomer().getCustomerId().equals(customerId)) {
                throw new RuntimeException("Access denied");
            }
        }
        return quotation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quotation> getMyQuotations() {
        Long customerId = currentUserService.requireCustomerId();
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return quotationRepo.findByCustomerWithDetails(customer);
    }

    @Override
    @Transactional
    public Quotation submit(Long id) {
        Quotation quotation = getById(id);
        if (!"draft".equalsIgnoreCase(quotation.getStatus())) {
            throw new RuntimeException("Only draft quotations can be submitted");
        }
        ensureCanModify(quotation);
        if (quotation.getItems() == null || quotation.getItems().isEmpty()) {
            throw new RuntimeException("Add at least one line item before submitting");
        }

        quotation.setStatus("submitted");
        Quotation saved = quotationRepo.save(quotation);
        activityLogService.log(currentUserService.getCurrentUser(), "QUOTATION_SUBMITTED", "QUOTATION", id,
                "Submitted quotation " + saved.getQuotationNumber());
        return quotationRepo.findByIdWithDetails(saved.getQuotationId()).orElse(saved);
    }

    @Override
    @Transactional
    public Quotation updateStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }
        String newStatus = status.trim().toLowerCase();
        if (!List.of("approved", "rejected").contains(newStatus)) {
            throw new RuntimeException("Status must be approved or rejected");
        }

        Quotation quotation = getById(id);
        String current = quotation.getStatus() != null ? quotation.getStatus().toLowerCase() : "";

        if ("converted".equals(current)) {
            throw new RuntimeException("Quotation is already converted to a bill");
        }

        if ("approved".equals(newStatus)) {
            if (!List.of("submitted", "draft").contains(current)) {
                throw new RuntimeException("Only draft or submitted quotations can be approved");
            }
        }

        if ("rejected".equals(newStatus)) {
            if (!List.of("submitted", "draft").contains(current)) {
                throw new RuntimeException("Only draft or submitted quotations can be rejected");
            }
        }

        quotation.setStatus(newStatus);
        Quotation saved = quotationRepo.save(quotation);
        activityLogService.log(currentUserService.getCurrentUser(), "QUOTATION_" + newStatus.toUpperCase(),
                "QUOTATION", id, newStatus + " quotation " + saved.getQuotationNumber());
        return quotationRepo.findByIdWithDetails(saved.getQuotationId()).orElse(saved);
    }

    @Override
    @Transactional
    public Quotation convertToBill(Long id) {
        Quotation quotation = getById(id);
        if ("converted".equalsIgnoreCase(quotation.getStatus())) {
            throw new RuntimeException("Quotation is already converted");
        }
        if (!List.of("submitted", "approved").contains(quotation.getStatus().toLowerCase())) {
            throw new RuntimeException("Only submitted or approved quotations can be converted to a bill");
        }
        if (quotation.getItems() == null || quotation.getItems().isEmpty()) {
            throw new RuntimeException("Quotation has no line items");
        }

        List<BillItem> billItems = new ArrayList<>();
        for (QuotationItem item : quotation.getItems()) {
            if (item.getDesign() == null) {
                throw new RuntimeException("Line item missing design");
            }
            if (item.getColor() == null || item.getColor().isBlank()) {
                throw new RuntimeException("Each line needs a color to convert to bill");
            }
            if (item.getSize() == null || item.getSize().getSizeId() == null) {
                throw new RuntimeException("Each line needs a size to convert to bill");
            }

            Design design = designRepo.findById(item.getDesign().getDesignId())
                    .orElseThrow(() -> new RuntimeException("Design not found"));
            Size size = sizeRepo.findById(item.getSize().getSizeId())
                    .orElseThrow(() -> new RuntimeException("Size not found"));

            Suit suit = suitRepo.findByDesignAndSizeAndColor(design, size, item.getColor())
                    .or(() -> suitRepo.findByDesignAndSizeIsNullAndColor(design, item.getColor()))
                    .orElseThrow(() -> new RuntimeException(
                            "No stock variant for " + design.getDesignCode() + " / "
                            + size.getSizeValue() + " / " + item.getColor()));

            Product product = productRepo.findBySuit(suit)
                    .orElseThrow(() -> new RuntimeException(
                            "No product for " + design.getDesignCode() + " / "
                            + size.getSizeValue() + " / " + item.getColor()));

            billItems.add(BillItem.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build());
        }

        Bill bill = Bill.builder()
                .customer(quotation.getCustomer())
                .createdBy(currentUserService.getCurrentUser())
                .discount(quotation.getDiscount() != null ? quotation.getDiscount() : BigDecimal.ZERO)
                .items(billItems)
                .build();

        Bill savedBill = billService.createBill(bill);

        quotation.setBill(savedBill);
        quotation.setStatus("converted");
        Quotation saved = quotationRepo.save(quotation);

        activityLogService.log(currentUserService.getCurrentUser(), "QUOTATION_CONVERTED", "QUOTATION", id,
                "Converted quotation " + saved.getQuotationNumber() + " to bill " + savedBill.getBillNumber());

        return quotationRepo.findByIdWithDetails(saved.getQuotationId()).orElse(saved);
    }

    @Override
    public String generateNextQuotationNumber() {
        long next = quotationRepo.count() + 1;
        return String.format("QUOTE-%d-%03d", LocalDate.now().getYear(), next);
    }

    private void resolveItems(Quotation quotation) {
        if (quotation.getItems() == null) return;
        for (QuotationItem item : quotation.getItems()) {
            if (item.getDesign() == null || item.getDesign().getDesignId() == null) {
                throw new RuntimeException("Each line item needs a design");
            }
            Design design = designRepo.findById(item.getDesign().getDesignId())
                    .orElseThrow(() -> new RuntimeException("Design not found"));
            item.setDesign(design);

            if (item.getSize() != null && item.getSize().getSizeId() != null) {
                Size size = sizeRepo.findById(item.getSize().getSizeId())
                        .orElseThrow(() -> new RuntimeException("Size not found"));
                item.setSize(size);
            } else {
                item.setSize(null);
            }

            if (item.getUnitPrice() == null || item.getUnitPrice().signum() <= 0) {
                item.setUnitPrice(design.getBasePrice() != null ? design.getBasePrice() : BigDecimal.ZERO);
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("Quantity must be greater than zero");
            }
            item.setQuotation(quotation);
        }
    }

    private void computeTotals(Quotation quotation, Customer customer) {
        BigDecimal total = quotation.getItems() == null ? BigDecimal.ZERO :
                quotation.getItems().stream()
                        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (quotation.getItems() != null) {
            quotation.getItems().forEach(item ->
                    item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        }

        BigDecimal discount = quotation.getDiscount();
        if (discount == null || discount.signum() == 0) {
            discount = customerDiscountAmount(total, customer.getDiscountPercent());
        }

        quotation.setTotalAmount(total);
        quotation.setDiscount(discount);
        quotation.setFinalAmount(total.subtract(discount).max(BigDecimal.ZERO));
    }

    private BigDecimal customerDiscountAmount(BigDecimal total, BigDecimal percent) {
        if (percent == null || percent.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return total.multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void ensureCanModify(Quotation quotation) {
        User user = currentUserService.getCurrentUser();
        if (!isCustomerRole(user)) return;
        Long customerId = currentUserService.requireCustomerId();
        if (!quotation.getCustomer().getCustomerId().equals(customerId)) {
            throw new RuntimeException("You can only modify your own quotations");
        }
    }

    private boolean isCustomerRole(User user) {
        return user.getRole() != null
                && "CUSTOMER".equalsIgnoreCase(user.getRole().getRoleName());
    }
}
