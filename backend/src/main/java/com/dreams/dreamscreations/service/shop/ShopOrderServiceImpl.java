package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopCheckoutRequest;
import com.dreams.dreamscreations.dto.shop.ShopOrderDTO;
import com.dreams.dreamscreations.dto.shop.ShopOrderItemDTO;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.entity.shop.ShopCart;
import com.dreams.dreamscreations.entity.shop.ShopCartItem;
import com.dreams.dreamscreations.entity.shop.ShopOrder;
import com.dreams.dreamscreations.entity.shop.ShopOrderItem;
import com.dreams.dreamscreations.repository.CustomerRepository;
import com.dreams.dreamscreations.repository.QuotationRepository;
import com.dreams.dreamscreations.repository.shop.ShopCartRepository;
import com.dreams.dreamscreations.repository.shop.ShopOrderRepository;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.ActivityLogService;
import com.dreams.dreamscreations.service.BillService;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopOrderServiceImpl implements ShopOrderService {

    private final ShopOrderRepository orderRepo;
    private final ShopCartRepository cartRepo;
    private final ShopCartService cartService;
    private final CustomerRepository customerRepo;
    private final QuotationRepository quotationRepo;
    private final BillService billService;
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;

    public ShopOrderServiceImpl(ShopOrderRepository orderRepo,
                                ShopCartRepository cartRepo,
                                ShopCartService cartService,
                                CustomerRepository customerRepo,
                                QuotationRepository quotationRepo,
                                BillService billService,
                                InventoryService inventoryService,
                                CurrentUserService currentUserService,
                                ActivityLogService activityLogService) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.cartService = cartService;
        this.customerRepo = customerRepo;
        this.quotationRepo = quotationRepo;
        this.billService = billService;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional
    public ShopOrderDTO checkout(Long customerId, ShopCheckoutRequest request) {
        ShopCart cart = cartRepo.findByCustomerIdWithItems(customerId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        for (ShopCartItem line : cart.getItems()) {
            Product product = line.getProduct();
            Suit suit = product != null ? product.getSuit() : null;
            if (suit == null) {
                throw new RuntimeException("Product variant not found");
            }
            int stock = inventoryService.getQuantity(suit);
            if (line.getQuantity() > stock) {
                throw new RuntimeException("Only " + stock + " available in stock for one or more items");
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<ShopOrderItem> orderItems = new ArrayList<>();
        for (ShopCartItem line : cart.getItems()) {
            BigDecimal lineTotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            orderItems.add(ShopOrderItem.builder()
                    .product(line.getProduct())
                    .quantity(line.getQuantity())
                    .unitPrice(line.getUnitPrice())
                    .totalPrice(lineTotal)
                    .build());
        }

        BigDecimal discountPct = nz(customer.getDiscountPercent());
        BigDecimal discountAmount = subtotal.multiply(discountPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        ShopOrder order = ShopOrder.builder()
                .orderNumber(generateNextOrderNumber())
                .customer(customer)
                .status("pending")
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(total)
                .shippingNotes(trimTo(request != null ? request.getShippingNotes() : null, 500))
                .customerNotes(trimTo(request != null ? request.getCustomerNotes() : null, 500))
                .build();

        for (ShopOrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        ShopOrder saved = orderRepo.save(order);
        cartService.clearCart(customerId);

        activityLogService.log(currentUserService.getCurrentUser(), "SHOP_ORDER_CREATED", "SHOP_ORDER",
                saved.getOrderId(), "Placed shop order " + saved.getOrderNumber());

        return orderRepo.findByIdWithDetails(saved.getOrderId())
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Order not found after checkout"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopOrderDTO> getMyOrders(Long customerId) {
        return orderRepo.findByCustomerIdWithDetails(customerId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderDTO getMyOrder(Long customerId, Long orderId) {
        return orderRepo.findByIdAndCustomerIdWithDetails(orderId, customerId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    @Transactional
    public ShopOrderDTO cancelMyOrder(Long customerId, Long orderId) {
        ShopOrder order = orderRepo.findByIdAndCustomerIdWithDetails(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!"pending".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }
        order.setStatus("cancelled");
        ShopOrder saved = orderRepo.save(order);
        activityLogService.log(currentUserService.getCurrentUser(), "SHOP_ORDER_CANCELLED", "SHOP_ORDER",
                orderId, "Customer cancelled order " + saved.getOrderNumber());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopOrderDTO> getAllOrders(String status) {
        return orderRepo.findAllWithDetails().stream()
                .filter(o -> status == null || status.isBlank()
                        || status.equalsIgnoreCase(o.getStatus()))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderDTO getOrder(Long orderId) {
        return orderRepo.findByIdWithDetails(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    @Transactional
    public ShopOrderDTO updateStatus(Long orderId, String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }
        ShopOrder order = orderRepo.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        String current = order.getStatus().toLowerCase();
        String next = status.trim().toLowerCase();

        if ("confirmed".equals(next)) {
            if (!"pending".equals(current)) {
                throw new RuntimeException("Only pending orders can be confirmed");
            }
            order.setConfirmedBy(currentUserService.getCurrentUser());
        } else if ("fulfilled".equals(next)) {
            if (!"confirmed".equals(current)) {
                throw new RuntimeException("Only confirmed orders can be marked fulfilled");
            }
        } else if ("cancelled".equals(next)) {
            if (List.of("fulfilled", "cancelled").contains(current)) {
                throw new RuntimeException("Order cannot be cancelled in status: " + current);
            }
        } else {
            throw new RuntimeException("Invalid status: " + status);
        }

        order.setStatus(next);
        ShopOrder saved = orderRepo.save(order);
        activityLogService.log(currentUserService.getCurrentUser(),
                "SHOP_ORDER_" + next.toUpperCase(), "SHOP_ORDER", orderId,
                "Updated shop order " + saved.getOrderNumber() + " to " + next);
        return toDto(saved);
    }

    @Override
    @Transactional
    public ShopOrderDTO convertToQuotation(Long orderId) {
        ShopOrder order = orderRepo.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getQuotation() != null) {
            throw new RuntimeException("Order already linked to a quotation");
        }
        if (!"confirmed".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Only confirmed orders can be converted to quotation");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order has no line items");
        }

        List<QuotationItem> quoteItems = new ArrayList<>();
        for (ShopOrderItem line : order.getItems()) {
            Product product = line.getProduct();
            Suit suit = product.getSuit();
            Design design = suit.getDesign();
            quoteItems.add(QuotationItem.builder()
                    .design(design)
                    .size(suit.getSize())
                    .color(suit.getColor())
                    .quantity(line.getQuantity())
                    .unitPrice(line.getUnitPrice())
                    .totalPrice(line.getTotalPrice())
                    .build());
        }

        Quotation quotation = Quotation.builder()
                .customer(order.getCustomer())
                .status("submitted")
                .discount(order.getDiscountAmount())
                .notes("From shop order " + order.getOrderNumber())
                .createdBy(currentUserService.getCurrentUser())
                .items(quoteItems)
                .build();

        for (QuotationItem item : quoteItems) {
            item.setQuotation(quotation);
        }

        computeQuotationTotals(quotation, order.getCustomer());
        quotation.setQuotationNumber(generateNextQuotationNumber());
        Quotation savedQuote = quotationRepo.save(quotation);

        order.setQuotation(savedQuote);
        orderRepo.save(order);

        activityLogService.log(currentUserService.getCurrentUser(), "SHOP_ORDER_TO_QUOTE", "SHOP_ORDER",
                orderId, "Converted shop order " + order.getOrderNumber()
                        + " to quotation " + savedQuote.getQuotationNumber());

        return getOrder(orderId);
    }

    @Override
    @Transactional
    public ShopOrderDTO convertToBill(Long orderId) {
        ShopOrder order = orderRepo.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getBill() != null) {
            throw new RuntimeException("Order already linked to a bill");
        }
        if (!"confirmed".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Only confirmed orders can be converted to bill");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order has no line items");
        }

        List<BillItem> billItems = new ArrayList<>();
        for (ShopOrderItem line : order.getItems()) {
            billItems.add(BillItem.builder()
                    .product(line.getProduct())
                    .quantity(line.getQuantity())
                    .unitPrice(line.getUnitPrice())
                    .build());
        }

        Bill bill = Bill.builder()
                .customer(order.getCustomer())
                .createdBy(currentUserService.getCurrentUser())
                .discount(order.getDiscountAmount())
                .items(billItems)
                .build();

        Bill savedBill = billService.createBill(bill);

        order.setBill(savedBill);
        order.setStatus("fulfilled");
        if (order.getConfirmedBy() == null) {
            order.setConfirmedBy(currentUserService.getCurrentUser());
        }
        orderRepo.save(order);

        activityLogService.log(currentUserService.getCurrentUser(), "SHOP_ORDER_TO_BILL", "SHOP_ORDER",
                orderId, "Converted shop order " + order.getOrderNumber()
                        + " to bill " + savedBill.getBillNumber());

        return getOrder(orderId);
    }

    @Override
    public String generateNextOrderNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "SHOP-" + year + "-";
        long next = orderRepo.countByOrderNumberStartingWith(prefix) + 1;
        return String.format("%s%03d", prefix, next);
    }

    private String generateNextQuotationNumber() {
        long next = quotationRepo.count() + 1;
        return String.format("QUOTE-%d-%03d", LocalDate.now().getYear(), next);
    }

    private void computeQuotationTotals(Quotation quotation, Customer customer) {
        BigDecimal total = quotation.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = quotation.getDiscount() != null ? quotation.getDiscount() : BigDecimal.ZERO;
        quotation.setTotalAmount(total);
        quotation.setDiscount(discount);
        quotation.setFinalAmount(total.subtract(discount).max(BigDecimal.ZERO));
    }

    private ShopOrderDTO toDto(ShopOrder order) {
        ShopOrderDTO dto = new ShopOrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingNotes(order.getShippingNotes());
        dto.setCustomerNotes(order.getCustomerNotes());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
            dto.setCustomerName(formatCustomerName(order.getCustomer()));
        }
        if (order.getQuotation() != null) {
            dto.setQuotationId(order.getQuotation().getQuotationId());
            dto.setQuotationNumber(order.getQuotation().getQuotationNumber());
        }
        if (order.getBill() != null) {
            dto.setBillId(order.getBill().getBillId());
            dto.setBillNumber(order.getBill().getBillNumber());
        }

        int count = 0;
        if (order.getItems() != null) {
            List<ShopOrderItem> sorted = order.getItems().stream()
                    .sorted(Comparator.comparing(i -> i.getProduct().getProductId()))
                    .toList();
            for (ShopOrderItem line : sorted) {
                ShopOrderItemDTO itemDto = new ShopOrderItemDTO();
                itemDto.setItemId(line.getItemId());
                itemDto.setProductId(line.getProduct().getProductId());
                itemDto.setQuantity(line.getQuantity());
                itemDto.setUnitPrice(line.getUnitPrice());
                itemDto.setTotalPrice(line.getTotalPrice());

                Product product = line.getProduct();
                Suit suit = product.getSuit();
                if (suit != null) {
                    itemDto.setColor(suit.getColor());
                    if (suit.getSize() != null) {
                        itemDto.setSizeValue(suit.getSize().getSizeValue());
                    }
                    Design design = suit.getDesign();
                    if (design != null) {
                        itemDto.setDesignId(design.getDesignId());
                        itemDto.setDesignCode(design.getDesignCode());
                        itemDto.setDesignName(design.getName());
                    }
                }
                dto.getItems().add(itemDto);
                count += line.getQuantity();
            }
        }
        dto.setItemCount(count);
        return dto;
    }

    private String formatCustomerName(Customer customer) {
        String first = customer.getFirstName() != null ? customer.getFirstName().trim() : "";
        String last = customer.getLastName() != null ? customer.getLastName().trim() : "";
        return (first + " " + last).trim();
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String trimTo(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
