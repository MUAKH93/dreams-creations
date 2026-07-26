package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.CustomerBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerBalanceRepository extends JpaRepository<CustomerBalance, Long> {
    Optional<CustomerBalance> findByCustomer_CustomerId(Long customerId);
}
