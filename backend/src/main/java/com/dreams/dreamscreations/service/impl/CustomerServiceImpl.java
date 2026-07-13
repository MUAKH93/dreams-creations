package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.PaymentReminderDTO;
import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.CustomerBalance;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.BillRepository;
import com.dreams.dreamscreations.repository.CustomerBalanceRepository;
import com.dreams.dreamscreations.repository.CustomerRepository;
import com.dreams.dreamscreations.repository.EmailVerificationTokenRepository;
import com.dreams.dreamscreations.repository.PasswordResetTokenRepository;
import com.dreams.dreamscreations.repository.QuotationRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import com.dreams.dreamscreations.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepo;
    private final CustomerBalanceRepository balanceRepo;
    private final BillRepository billRepo;
    private final QuotationRepository quotationRepo;
    private final UserRepository userRepo;
    private final EmailVerificationTokenRepository emailVerifyTokenRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;

    public CustomerServiceImpl(CustomerRepository customerRepo,
                               CustomerBalanceRepository balanceRepo,
                               BillRepository billRepo,
                               QuotationRepository quotationRepo,
                               UserRepository userRepo,
                               EmailVerificationTokenRepository emailVerifyTokenRepo,
                               PasswordResetTokenRepository passwordResetTokenRepo) {
        this.customerRepo = customerRepo;
        this.balanceRepo = balanceRepo;
        this.billRepo = billRepo;
        this.quotationRepo = quotationRepo;
        this.userRepo = userRepo;
        this.emailVerifyTokenRepo = emailVerifyTokenRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
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
        if (updated.getDiscountPercent() != null) {
            existing.setDiscountPercent(updated.getDiscountPercent());
        }
        return customerRepo.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = getById(id);
        if (billRepo.countByCustomer_CustomerId(id) > 0) {
            throw new RuntimeException("Cannot delete customer with existing bills");
        }

        quotationRepo.deleteAll(quotationRepo.findByCustomer_CustomerId(id));
        balanceRepo.findByCustomer_CustomerId(id).ifPresent(balanceRepo::delete);

        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            userRepo.findCustomerUserByEmail(customer.getEmail()).ifPresent(this::deleteCustomerLogin);
        }

        customerRepo.delete(customer);
    }

    private void deleteCustomerLogin(User user) {
        emailVerifyTokenRepo.deleteByUser_UserId(user.getUserId());
        passwordResetTokenRepo.deleteByUser_UserId(user.getUserId());
        userRepo.delete(user);
    }

    @Override
    public CustomerBalance getBalance(Long customerId) {
        return balanceRepo.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Balance not found for customer: " + customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentReminderDTO> getPaymentReminders(int overdueDays) {
        if (overdueDays < 1) {
            overdueDays = 30;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(overdueDays);
        List<Bill> overdueBills = billRepo.findOverdueBills(cutoff);

        Map<Long, List<Bill>> byCustomer = new LinkedHashMap<>();
        for (Bill bill : overdueBills) {
            Long customerId = bill.getCustomer().getCustomerId();
            byCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(bill);
        }

        List<PaymentReminderDTO> reminders = new ArrayList<>();
        for (Map.Entry<Long, List<Bill>> entry : byCustomer.entrySet()) {
            List<Bill> bills = entry.getValue();
            Bill oldest = bills.stream()
                    .min(Comparator.comparing(Bill::getBillDate))
                    .orElse(bills.get(0));
            Customer customer = oldest.getCustomer();
            BigDecimal balanceDue = balanceRepo.findByCustomer_CustomerId(customer.getCustomerId())
                    .map(b -> b.getBalance() != null ? b.getBalance() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);

            if (balanceDue.signum() <= 0) {
                continue;
            }

            long days = oldest.getBillDate() != null
                    ? ChronoUnit.DAYS.between(oldest.getBillDate(), LocalDateTime.now())
                    : overdueDays;

            reminders.add(PaymentReminderDTO.builder()
                    .customerId(customer.getCustomerId())
                    .customerName(customer.getFirstName() + " " + customer.getLastName())
                    .phone(customer.getPhone())
                    .balanceDue(balanceDue)
                    .overdueBillCount(bills.size())
                    .oldestBillDate(oldest.getBillDate())
                    .daysOverdue((int) days)
                    .build());
        }

        reminders.sort(Comparator.comparingInt(PaymentReminderDTO::getDaysOverdue).reversed());
        return reminders;
    }
}
