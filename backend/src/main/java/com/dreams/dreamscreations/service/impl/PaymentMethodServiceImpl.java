package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.PaymentMethod;
import com.dreams.dreamscreations.repository.PaymentMethodRepository;
import com.dreams.dreamscreations.service.PaymentMethodService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository repo;

    public PaymentMethodServiceImpl(PaymentMethodRepository repo) { this.repo = repo; }

    @Override public PaymentMethod save(PaymentMethod pm) { return repo.save(pm); }
    @Override public List<PaymentMethod> getAll() { return repo.findAll(); }

    @Override
    public PaymentMethod getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + id));
    }
}
