package com.dreams.dreamscreations.dto.integration;

public class ShopOrderSyncResponse {

    private Long billId;
    private String billNumber;
    private String billStatus;
    private Long paymentId;
    private boolean alreadySynced;

    public ShopOrderSyncResponse() {
    }

    public ShopOrderSyncResponse(Long billId, String billNumber, String billStatus,
                                 Long paymentId, boolean alreadySynced) {
        this.billId = billId;
        this.billNumber = billNumber;
        this.billStatus = billStatus;
        this.paymentId = paymentId;
        this.alreadySynced = alreadySynced;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public boolean isAlreadySynced() {
        return alreadySynced;
    }

    public void setAlreadySynced(boolean alreadySynced) {
        this.alreadySynced = alreadySynced;
    }
}
