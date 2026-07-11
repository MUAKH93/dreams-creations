package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.CustomerBalance;
import java.util.List;
import java.util.Optional;
import java.util.Optional;

public interface CustomerService {
    Customer save(Customer customer);
    List<Customer> getAll();
    Customer getById(Long id);
    Optional<Customer> findByEmail(String email);
    Customer update(Long id, Customer customer);
    void delete(Long id);
    CustomerBalance getBalance(Long customerId);
}
