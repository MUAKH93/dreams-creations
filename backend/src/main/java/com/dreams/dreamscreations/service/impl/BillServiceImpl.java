package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.service.ActivityLogService;
import com.dreams.dreamscreations.service.BillService;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepo;
    private final BillItemRepository billItemRepo;
    private final CustomerRepository customerRepo;
    private final CustomerBalanceRepository balanceRepo;
    private final ProductRepository productRepo;
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;

    public BillServiceImpl(BillRepository billRepo,
                           BillItemRepository billItemRepo,
                           CustomerRepository customerRepo,
                           CustomerBalanceRepository balanceRepo,
                           ProductRepository productRepo,
                           InventoryService inventoryService,
                           CurrentUserService currentUserService,
                           ActivityLogService activityLogService) {
        this.billRepo = billRepo;
        this.billItemRepo = billItemRepo;
        this.customerRepo = customerRepo;
        this.balanceRepo = balanceRepo;
        this.productRepo = productRepo;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional
    public Bill createBill(Bill bill) {

        if (bill.getBillNumber() == null || bill.getBillNumber().isBlank()) {
            bill.setBillNumber(generateNextBillNumber());
        }

        if (billRepo.findByBillNumber(bill.getBillNumber()).isPresent()) {
            throw new RuntimeException("Bill number already exists: " + bill.getBillNumber());
        }

        if (bill.getItems() != null) {
            bill.getItems().forEach(item -> {
                BigDecimal lineTotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                item.setTotalPrice(lineTotal);
                item.setBill(bill);
            });
            validateStockAvailability(bill.getItems());
        }

        BigDecimal total = bill.getItems() == null ? BigDecimal.ZERO :
                bill.getItems().stream()
                        .map(BillItem::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = bill.getDiscount() != null
                ? bill.getDiscount() : BigDecimal.ZERO;

        bill.setTotalAmount(total);
        bill.setDiscount(discount);
        bill.setFinalAmount(total.subtract(discount));

        Bill saved = billRepo.save(bill);
        billRepo.flush();

        deductStockForBill(saved);

        updateCustomerBalance(saved.getCustomer().getCustomerId());

        return billRepo.findById(saved.getBillId()).orElse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> getAll() { return billRepo.findAllWithDetails(); }

    @Override
    @Transactional(readOnly = true)
    public Bill getById(Long id) {
        return billRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
    }

    @Override
    public List<Bill> getByCustomerId(Long customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return billRepo.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> getMyBills() {
        Long customerId = currentUserService.requireCustomerId();
        return getByCustomerId(customerId);
    }

    @Override
    public List<Bill> getByStatus(String status) {
        return billRepo.findByStatus(status);
    }

    @Override
    @Transactional
    public Bill updateStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        Bill bill = getById(id);
        String previousStatus = bill.getStatus();
        String newStatus = status.trim().toLowerCase();

        if ("cancelled".equalsIgnoreCase(newStatus)) {
            if ("cancelled".equalsIgnoreCase(previousStatus)) {
                throw new RuntimeException("Bill is already cancelled");
            }
            if ("paid".equalsIgnoreCase(previousStatus)) {
                throw new RuntimeException("Cannot cancel a paid bill");
            }
            bill.setStatus("cancelled");
            Bill saved = billRepo.save(bill);
            restoreStockForBill(saved);
            updateCustomerBalance(saved.getCustomer().getCustomerId());
            activityLogService.log(currentUserService.getCurrentUser(), "BILL_CANCELLED", "BILL", id,
                    "Cancelled bill " + saved.getBillNumber());
            return billRepo.findById(saved.getBillId()).orElse(saved);
        }

        if ("cancelled".equalsIgnoreCase(previousStatus)) {
            throw new RuntimeException("Cannot change status of a cancelled bill");
        }

        bill.setStatus(newStatus);
        Bill saved = billRepo.save(bill);
        updateCustomerBalance(saved.getCustomer().getCustomerId());
        return saved;
    }

    private void validateStockAvailability(List<BillItem> items) {
        for (BillItem item : items) {
            Product product = resolveProduct(item);
            int available = inventoryService.getQuantity(product.getSuit());
            if (available < item.getQuantity()) {
                Suit suit = product.getSuit();
                throw new RuntimeException(
                        "Insufficient stock for " + suit.getDesign().getDesignCode()
                        + " / " + (suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD")
                        + " / " + suit.getColor()
                        + ": only " + available + " available, bill requests " + item.getQuantity()
                );
            }
        }
    }

    private void deductStockForBill(Bill bill) {
        List<BillItem> items = bill.getItems();
        if (items == null || items.isEmpty()) {
            items = billItemRepo.findByBill(bill);
        }
        for (BillItem item : items) {
            Product product = resolveProduct(item);
            inventoryService.removeStock(product.getSuit(), item.getQuantity());
        }
    }

    private void restoreStockForBill(Bill bill) {
        List<BillItem> items = bill.getItems();
        if (items == null || items.isEmpty()) {
            items = billItemRepo.findByBill(bill);
        }
        for (BillItem item : items) {
            Product product = resolveProduct(item);
            inventoryService.restoreStock(product.getSuit(), item.getQuantity());
        }
    }

    private Product resolveProduct(BillItem item) {
        Long productId = item.getProduct().getProductId();
        return productRepo.findByIdWithSuit(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    public void updateCustomerBalance(Long customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        BigDecimal totalSales = billRepo.sumFinalAmountByCustomer(customerId);

        CustomerBalance balance = balanceRepo.findByCustomer_CustomerId(customerId)
                .orElse(CustomerBalance.builder().customer(customer).build());

        balance.setTotalSales(totalSales);
        balance.setBalance(totalSales.subtract(balance.getTotalPaid()));
        balanceRepo.save(balance);
    }

    @Override
    public String generateNextBillNumber() {
        long next = billRepo.count() + 1;
        return String.format("BILL-%d-%03d", LocalDate.now().getYear(), next);
    }
}
