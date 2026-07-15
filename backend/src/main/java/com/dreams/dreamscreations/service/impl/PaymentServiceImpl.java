package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final BillRepository billRepo;
    private final CustomerBalanceRepository balanceRepo;
    private final CustomerRepository customerRepo;

    public PaymentServiceImpl(PaymentRepository paymentRepo,
                              BillRepository billRepo,
                              CustomerBalanceRepository balanceRepo,
                              CustomerRepository customerRepo) {
        this.paymentRepo = paymentRepo;
        this.billRepo = billRepo;
        this.balanceRepo = balanceRepo;
        this.customerRepo = customerRepo;
    }

    /**
     * Records a payment against a bill.
     * Any payment (including partial) closes the bill as "paid".
     * Remaining customer balance is tracked on CustomerBalance.
     */
    @Override
    @Transactional
    public Payment recordPayment(Payment payment) {

        Bill bill = billRepo.findById(payment.getBill().getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if ("cancelled".equalsIgnoreCase(bill.getStatus())) {
            throw new RuntimeException("Cannot record payment on a cancelled bill");
        }
        if ("paid".equalsIgnoreCase(bill.getStatus())) {
            throw new RuntimeException("This bill is already closed — record further payments from Customer Records");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        Payment saved = paymentRepo.save(payment);
        saved = paymentRepo.findById(saved.getPaymentId()).orElse(saved);

        bill.setStatus("paid");
        billRepo.save(bill);

        Long customerId = bill.getCustomer().getCustomerId();
        BigDecimal totalPaidByCustomer = paymentRepo.sumAmountByCustomer(customerId);

        CustomerBalance balance = balanceRepo.findByCustomer_CustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerRepo.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Customer not found"));
                    return CustomerBalance.builder().customer(customer).build();
                });

        balance.setTotalPaid(totalPaidByCustomer);
        balance.setBalance(balance.getTotalSales().subtract(totalPaidByCustomer));
        balanceRepo.save(balance);

        return saved;
    }

    @Override
    public List<Payment> getByBillId(Long billId) {
        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + billId));
        return paymentRepo.findByBill(bill);
    }

    @Override
    public List<Payment> getByCustomerId(Long customerId) {
        customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return paymentRepo.findByCustomerWithDetails(customerId);
    }

    @Override
    public List<Payment> getAll() {
        return paymentRepo.findAll();
    }
}
