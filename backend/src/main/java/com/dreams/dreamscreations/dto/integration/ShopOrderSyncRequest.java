package com.dreams.dreamscreations.dto.integration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Shop order payload for ERP bill creation and finance auto-posting.
 */
public class ShopOrderSyncRequest {

    private Long shopOrderId;
    private String orderNumber;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal discount;
    private List<ShopOrderLineRequest> items;
    /** When set, records a payment after bill creation (e.g. online checkout). */
    private BigDecimal paidAmount;
    private String paymentReference;

    public Long getShopOrderId() {
        return shopOrderId;
    }

    public void setShopOrderId(Long shopOrderId) {
        this.shopOrderId = shopOrderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public List<ShopOrderLineRequest> getItems() {
        return items;
    }

    public void setItems(List<ShopOrderLineRequest> items) {
        this.items = items;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
}
