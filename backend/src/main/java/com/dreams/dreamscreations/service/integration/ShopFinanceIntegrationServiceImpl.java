package com.dreams.dreamscreations.service.integration;

import com.dreams.dreamscreations.dto.integration.ShopOrderLineRequest;
import com.dreams.dreamscreations.dto.integration.ShopOrderSyncRequest;
import com.dreams.dreamscreations.dto.integration.ShopOrderSyncResponse;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.service.BillService;
import com.dreams.dreamscreations.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShopFinanceIntegrationServiceImpl implements ShopFinanceIntegrationService {

    private static final String BILL_PREFIX = "SHOP-";

    private final BillRepository billRepo;
    private final BillService billService;
    private final PaymentService paymentService;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final PaymentMethodRepository paymentMethodRepo;

    public ShopFinanceIntegrationServiceImpl(BillRepository billRepo,
                                             BillService billService,
                                             PaymentService paymentService,
                                             CustomerRepository customerRepo,
                                             ProductRepository productRepo,
                                             UserRepository userRepo,
                                             PaymentMethodRepository paymentMethodRepo) {
        this.billRepo = billRepo;
        this.billService = billService;
        this.paymentService = paymentService;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.paymentMethodRepo = paymentMethodRepo;
    }

    @Override
    @Transactional
    public ShopOrderSyncResponse syncShopOrder(ShopOrderSyncRequest request) {
        validateRequest(request);

        String billNumber = BILL_PREFIX + request.getOrderNumber().trim();
        var existing = billRepo.findByBillNumber(billNumber);
        if (existing.isPresent()) {
            Bill bill = existing.get();
            return new ShopOrderSyncResponse(
                    bill.getBillId(),
                    bill.getBillNumber(),
                    bill.getStatus(),
                    null,
                    true
            );
        }

        Customer customer = resolveCustomer(request);
        User createdBy = userRepo.findAllByRoleName("ADMIN").stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No admin user found for shop integration"));

        List<BillItem> items = buildBillItems(request.getItems());
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .customer(customer)
                .discount(nz(request.getDiscount()))
                .createdBy(createdBy)
                .items(items)
                .build();

        Bill saved = billService.createBill(bill);

        Long paymentId = null;
        BigDecimal paid = request.getPaidAmount();
        if (paid != null && paid.compareTo(BigDecimal.ZERO) > 0) {
            PaymentMethod method = paymentMethodRepo.findByStatus("active").stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active payment method for shop payment sync"));

            Payment payment = Payment.builder()
                    .bill(saved)
                    .amount(paid)
                    .paymentMethod(method)
                    .referenceNo(request.getPaymentReference())
                    .notes("Shop order " + request.getOrderNumber())
                    .build();
            Payment recorded = paymentService.recordPayment(payment);
            paymentId = recorded.getPaymentId();
            saved = billRepo.findById(saved.getBillId()).orElse(saved);
        }

        return new ShopOrderSyncResponse(
                saved.getBillId(),
                saved.getBillNumber(),
                saved.getStatus(),
                paymentId,
                false
        );
    }

    private void validateRequest(ShopOrderSyncRequest request) {
        if (request == null || request.getOrderNumber() == null || request.getOrderNumber().isBlank()) {
            throw new RuntimeException("orderNumber is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("At least one order line is required");
        }
    }

    private Customer resolveCustomer(ShopOrderSyncRequest request) {
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            var byEmail = customerRepo.findFirstByEmail(request.getCustomerEmail().trim());
            if (byEmail.isPresent()) {
                return byEmail.get();
            }
        }
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            var byPhone = customerRepo.findByPhone(request.getCustomerPhone().trim());
            if (byPhone.isPresent()) {
                return byPhone.get();
            }
        }

        String firstName = request.getCustomerFirstName();
        if (firstName == null || firstName.isBlank()) {
            firstName = "Shop";
        }
        Customer created = Customer.builder()
                .firstName(firstName.trim())
                .lastName(trimOrNull(request.getCustomerLastName()))
                .email(trimOrNull(request.getCustomerEmail()))
                .phone(trimOrNull(request.getCustomerPhone()))
                .status("active")
                .build();
        return customerRepo.save(created);
    }

    private List<BillItem> buildBillItems(List<ShopOrderLineRequest> lines) {
        List<BillItem> items = new ArrayList<>();
        for (ShopOrderLineRequest line : lines) {
            if (line.getProductId() == null) {
                throw new RuntimeException("Each line requires productId (ERP catalog id)");
            }
            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new RuntimeException("Invalid quantity for product " + line.getProductId());
            }
            Product product = productRepo.findById(line.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + line.getProductId()));

            BigDecimal unitPrice = line.getUnitPrice() != null
                    ? line.getUnitPrice()
                    : (product.getSellingPrice() != null ? product.getSellingPrice() : BigDecimal.ZERO);

            items.add(BillItem.builder()
                    .product(product)
                    .quantity(line.getQuantity())
                    .unitPrice(unitPrice)
                    .build());
        }
        return items;
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
