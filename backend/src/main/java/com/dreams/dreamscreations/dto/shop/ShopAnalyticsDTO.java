package com.dreams.dreamscreations.dto.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopAnalyticsDTO {

    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long fulfilledOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalPaid = BigDecimal.ZERO;
    private BigDecimal outstanding = BigDecimal.ZERO;
    private List<TopDesignRow> topDesigns = new ArrayList<>();

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getConfirmedOrders() {
        return confirmedOrders;
    }

    public void setConfirmedOrders(long confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }

    public long getFulfilledOrders() {
        return fulfilledOrders;
    }

    public void setFulfilledOrders(long fulfilledOrders) {
        this.fulfilledOrders = fulfilledOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public void setOutstanding(BigDecimal outstanding) {
        this.outstanding = outstanding;
    }

    public List<TopDesignRow> getTopDesigns() {
        return topDesigns;
    }

    public void setTopDesigns(List<TopDesignRow> topDesigns) {
        this.topDesigns = topDesigns;
    }

    public static class TopDesignRow {
        private Long designId;
        private String designCode;
        private String designName;
        private long quantitySold;
        private BigDecimal revenue = BigDecimal.ZERO;

        public Long getDesignId() {
            return designId;
        }

        public void setDesignId(Long designId) {
            this.designId = designId;
        }

        public String getDesignCode() {
            return designCode;
        }

        public void setDesignCode(String designCode) {
            this.designCode = designCode;
        }

        public String getDesignName() {
            return designName;
        }

        public void setDesignName(String designName) {
            this.designName = designName;
        }

        public long getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(long quantitySold) {
            this.quantitySold = quantitySold;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }
}
