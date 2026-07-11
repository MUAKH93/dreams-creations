package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByStatus(String status);
}
