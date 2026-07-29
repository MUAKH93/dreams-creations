package com.dreams.dreamscreations.dto.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopCheckoutRequest {

    private String shippingNotes;
    private String customerNotes;

    public String getShippingNotes() {
        return shippingNotes;
    }

    public void setShippingNotes(String shippingNotes) {
        this.shippingNotes = shippingNotes;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }
}
