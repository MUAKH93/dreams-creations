package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinanceVendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceVendorRepository extends JpaRepository<FinanceVendor, Long> {

    List<FinanceVendor> findByIsActiveTrueOrderByVendorNameAsc();

    List<FinanceVendor> findAllByOrderByVendorNameAsc();
}
