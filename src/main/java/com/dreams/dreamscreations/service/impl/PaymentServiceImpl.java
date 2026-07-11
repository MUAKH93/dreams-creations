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
     *
     * Business rules:
     * 1. Payment amount cannot exceed the remaining balance on the bill
     * 2. Bill status is updated automatically:
     *    totalPaid >= finalAmount → "paid"
     *    totalPaid > 0            → "partial"
     * 3. CustomerBalance is updated immediately
     */
    @Override
    @Transactional
    public Payment recordPayment(Payment payment) {

        Bill bill = billRepo.findById(payment.getBill().getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if ("cancelled".equalsIgnoreCase(bill.getStatus())) {
            throw new RuntimeException("Cannot record payment on a cancelled bill");
        }

        // Rule 1: payment cannot exceed remaining balance
        BigDecimal alreadyPaid = paymentRepo.sumAmountByBill(bill.getBillId());
        BigDecimal remaining   = bill.getFinalAmount().subtract(alreadyPaid);

        if (payment.getAmount().compareTo(remaining) > 0) {
            throw new RuntimeException(
                "Payment amount " + payment.getAmount() +
                " exceeds remaining balance " + remaining
            );
        }

        Payment saved = paymentRepo.save(payment);
        // Re-fetch so the response has all fields populated (not lazy proxy nulls)
        saved = paymentRepo.findById(saved.getPaymentId()).orElse(saved);

        // Rule 2: update bill status
        BigDecimal newTotalPaid = alreadyPaid.add(payment.getAmount());
        if (newTotalPaid.compareTo(bill.getFinalAmount()) >= 0) {
            bill.setStatus("paid");
        } else {
            bill.setStatus("partial");
        }
        billRepo.save(bill);

        // Rule 3: update customer balance
        // sumAmountByCustomer already includes the payment we just saved above,
        // so we use it directly — do NOT add payment.getAmount() again
        Long customerId = bill.getCustomer().getCustomerId();
        BigDecimal totalPaidByCustomer = paymentRepo.sumAmountByCustomer(customerId);

        CustomerBalance balance = balanceRepo.findByCustomer_CustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerRepo.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Customer not found"));
                    return CustomerBalance.builder().customer(customer).build();
                });

        balance.setTotalPaid(totalPaidByCustomer);   // already the correct total, no need to add again
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
    public List<Payment> getAll() {
        return paymentRepo.findAll();
    }
}
