package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.PaymentMethod;
import java.util.List;

public interface PaymentMethodService {
    PaymentMethod save(PaymentMethod paymentMethod);
    List<PaymentMethod> getAll();
    PaymentMethod getById(Long id);
}
