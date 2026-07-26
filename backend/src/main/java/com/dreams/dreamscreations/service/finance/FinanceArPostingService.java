package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Payment;

public interface FinanceArPostingService {

    void postBillSale(Bill bill);

    void voidBillSale(Bill bill);

    void postPayment(Payment payment);
}
