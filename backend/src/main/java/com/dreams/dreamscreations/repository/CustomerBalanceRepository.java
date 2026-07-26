package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.CustomerBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerBalanceRepository extends JpaRepository<CustomerBalance, Long> {
    Optional<CustomerBalance> findByCustomer_CustomerId(Long customerId);

    @Query("SELECT cb FROM CustomerBalance cb JOIN FETCH cb.customer")
    List<CustomerBalance> findAllWithCustomer();
}
