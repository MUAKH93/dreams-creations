package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinancePayable;
import com.dreams.dreamscreations.entity.finance.FinancePayablePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancePayablePaymentRepository extends JpaRepository<FinancePayablePayment, Long> {

    long countByPayable(FinancePayable payable);
}
