package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinancePayable;
import com.dreams.dreamscreations.entity.finance.FinanceVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FinancePayableRepository extends JpaRepository<FinancePayable, Long> {

    @Query("SELECT p FROM FinancePayable p JOIN FETCH p.vendor JOIN FETCH p.expenseAccount ORDER BY p.invoiceDate DESC")
    List<FinancePayable> findAllWithDetails();

    @Query("SELECT p FROM FinancePayable p JOIN FETCH p.vendor JOIN FETCH p.expenseAccount WHERE p.payableId = :id")
    Optional<FinancePayable> findByIdWithDetails(Long id);

    @Query("SELECT p FROM FinancePayable p JOIN FETCH p.vendor WHERE p.status IN ('unpaid', 'partial') ORDER BY p.dueDate")
    List<FinancePayable> findOpenWithVendor();

    boolean existsByVendorAndInvoiceNumber(FinanceVendor vendor, String invoiceNumber);
}
