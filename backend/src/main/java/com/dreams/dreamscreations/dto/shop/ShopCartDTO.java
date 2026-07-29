package com.dreams.dreamscreations.dto.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopCartDTO {

    private Long cartId;
    private int itemCount;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private List<ShopCartItemDTO> items = new ArrayList<>();

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ShopCartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ShopCartItemDTO> items) {
        this.items = items;
    }
}
