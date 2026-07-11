package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.CustomerBalance;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;
    private final CurrentUserService currentUserService;

    public CustomerController(CustomerService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(customer));
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @GetMapping("/me/balance")
    public ResponseEntity<CustomerBalance> getMyBalance() {
        Long customerId = currentUserService.requireCustomerId();
        return ResponseEntity.ok(service.getBalance(customerId));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<CustomerBalance> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(service.getBalance(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        return ResponseEntity.ok(service.update(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
