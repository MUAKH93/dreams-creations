package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.shop.ShopOrder;
import com.dreams.dreamscreations.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopOrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ShopOrderNotificationService.class);

    private final EmailService emailService;

    public ShopOrderNotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notifyOrderPlaced(ShopOrder order) {
        send(order, "Order received — " + order.getOrderNumber(),
                "Thank you for your order.\n\n"
                        + "Order: " + order.getOrderNumber() + "\n"
                        + "Total: Rs. " + order.getTotalAmount() + "\n"
                        + "Status: pending review\n\n"
                        + "We will confirm your order shortly.");
    }

    public void notifyOrderConfirmed(ShopOrder order) {
        send(order, "Order confirmed — " + order.getOrderNumber(),
                "Your shop order has been confirmed.\n\n"
                        + "Order: " + order.getOrderNumber() + "\n"
                        + "Total: Rs. " + order.getTotalAmount() + "\n\n"
                        + "We are preparing your items.");
    }

    public void notifyOrderFulfilled(ShopOrder order) {
        send(order, "Order fulfilled — " + order.getOrderNumber(),
                "Your shop order is ready / on its way.\n\n"
                        + "Order: " + order.getOrderNumber() + "\n"
                        + "Total: Rs. " + order.getTotalAmount() + "\n\n"
                        + "Thank you for shopping with us.");
    }

    public void notifyOrderCancelled(ShopOrder order) {
        send(order, "Order cancelled — " + order.getOrderNumber(),
                "Your shop order has been cancelled.\n\n"
                        + "Order: " + order.getOrderNumber() + "\n\n"
                        + "Contact us if you have questions.");
    }

    private void send(ShopOrder order, String subject, String body) {
        Customer customer = order.getCustomer();
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            return;
        }
        if (!emailService.isConfigured()) {
            log.info("Shop email skipped (mail not configured): {} — {}", customer.getEmail(), subject);
            return;
        }
        try {
            emailService.send(customer.getEmail().trim(), subject, body);
        } catch (Exception ex) {
            log.warn("Failed to send shop order email to {}: {}", customer.getEmail(), ex.getMessage());
        }
    }
}
