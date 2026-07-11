package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.CustomerBalance;
import com.dreams.dreamscreations.repository.BillRepository;
import com.dreams.dreamscreations.repository.CustomerBalanceRepository;
import com.dreams.dreamscreations.repository.CustomerRepository;
import com.dreams.dreamscreations.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepo;
    private final CustomerBalanceRepository balanceRepo;
    private final BillRepository billRepo;

    public CustomerServiceImpl(CustomerRepository customerRepo,
                               CustomerBalanceRepository balanceRepo,
                               BillRepository billRepo) {
        this.customerRepo = customerRepo;
        this.balanceRepo = balanceRepo;
        this.billRepo = billRepo;
    }

    @Override
    public Customer save(Customer customer) {
        Customer saved = customerRepo.save(customer);
        // Create an empty balance record for every new customer
        CustomerBalance balance = CustomerBalance.builder()
                .customer(saved)
                .totalSales(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();
        balanceRepo.save(balance);
        return saved;
    }

    @Override public List<Customer> getAll() { return customerRepo.findAll(); }

    @Override
    public Customer getById(Long id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return customerRepo.findFirstByEmail(email);
    }

    @Override
    public Customer update(Long id, Customer updated) {
        Customer existing = getById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setStatus(updated.getStatus());
        return customerRepo.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = getById(id);
        if (billRepo.countByCustomer_CustomerId(id) > 0) {
            throw new RuntimeException("Cannot delete customer with existing bills");
        }
        balanceRepo.findByCustomer_CustomerId(id).ifPresent(balanceRepo::delete);
        customerRepo.delete(customer);
    }

    @Override
    public CustomerBalance getBalance(Long customerId) {
        return balanceRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Balance not found for customer: " + customerId));
    }
}
