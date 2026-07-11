package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Payment;
import java.util.List;

public interface PaymentService {
    Payment recordPayment(Payment payment);
    List<Payment> getByBillId(Long billId);
    List<Payment> getAll();
}
