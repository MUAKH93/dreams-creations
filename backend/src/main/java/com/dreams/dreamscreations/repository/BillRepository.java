package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    @Query("SELECT b FROM Bill b JOIN FETCH b.customer LEFT JOIN FETCH b.items")
    List<Bill> findAllWithDetails();

    @Query("SELECT DISTINCT b FROM Bill b " +
           "JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH p.suit s " +
           "LEFT JOIN FETCH s.design " +
           "LEFT JOIN FETCH s.size " +
           "WHERE b.billId = :id")
    Optional<Bill> findByIdWithDetails(@Param("id") Long id);

    List<Bill> findByCustomer(Customer customer);
    List<Bill> findByStatus(String status);
    long countByStatus(String status);
    long countByCustomer_CustomerId(Long customerId);
    Optional<Bill> findByBillNumber(String billNumber);

    // Total billed to a customer (excludes cancelled bills)
    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Bill b " +
           "WHERE b.customer.customerId = :customerId AND b.status <> 'cancelled'")
    BigDecimal sumFinalAmountByCustomer(@Param("customerId") Long customerId);
}
