package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBill(Bill bill);

    // Total paid against a specific bill
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.bill.billId = :billId")
    BigDecimal sumAmountByBill(@Param("billId") Long billId);

    // Total paid by a customer across all bills
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.bill.customer.customerId = :customerId")
    BigDecimal sumAmountByCustomer(@Param("customerId") Long customerId);
}
