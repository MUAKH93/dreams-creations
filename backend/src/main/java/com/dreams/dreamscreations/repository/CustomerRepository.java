package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAllByEmail(String email);
    List<Customer> findByStatus(String status);

    default Optional<Customer> findFirstByEmail(String email) {
        return findAllByEmail(email).stream().findFirst();
    }
}
