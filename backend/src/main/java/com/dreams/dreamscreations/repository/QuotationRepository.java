package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    @Query("SELECT DISTINCT q FROM Quotation q " +
           "JOIN FETCH q.customer " +
           "LEFT JOIN FETCH q.items i " +
           "LEFT JOIN FETCH i.design " +
           "LEFT JOIN FETCH i.size " +
           "ORDER BY q.createdAt DESC")
    List<Quotation> findAllWithDetails();

    @Query("SELECT DISTINCT q FROM Quotation q " +
           "JOIN FETCH q.customer " +
           "LEFT JOIN FETCH q.items i " +
           "LEFT JOIN FETCH i.design " +
           "LEFT JOIN FETCH i.size " +
           "WHERE q.quotationId = :id")
    Optional<Quotation> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT q FROM Quotation q " +
           "JOIN FETCH q.customer " +
           "LEFT JOIN FETCH q.items i " +
           "LEFT JOIN FETCH i.design " +
           "LEFT JOIN FETCH i.size " +
           "WHERE q.customer = :customer " +
           "ORDER BY q.createdAt DESC")
    List<Quotation> findByCustomerWithDetails(Customer customer);

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    long count();
    long countByStatus(String status);

    List<Quotation> findByCustomer_CustomerId(Long customerId);
}
