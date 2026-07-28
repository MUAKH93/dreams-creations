package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinanceBankAccount;
import com.dreams.dreamscreations.entity.finance.FinanceBankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface FinanceBankTransactionRepository extends JpaRepository<FinanceBankTransaction, Long> {

    @Query("SELECT t FROM FinanceBankTransaction t WHERE t.bankAccount.bankAccountId = :bankAccountId ORDER BY t.transactionDate DESC, t.transactionId DESC")
    List<FinanceBankTransaction> findByBankAccountId(Long bankAccountId);

    @Query("SELECT t FROM FinanceBankTransaction t WHERE t.bankAccount.bankAccountId = :bankAccountId AND t.transactionDate <= :asOfDate ORDER BY t.transactionDate, t.transactionId")
    List<FinanceBankTransaction> findByBankAccountIdAsOf(Long bankAccountId, LocalDate asOfDate);

    long countByBankAccountAndIsReconciledFalse(FinanceBankAccount bankAccount);

    List<FinanceBankTransaction> findByBankAccountAndIsReconciledFalseOrderByTransactionDateAsc(
            FinanceBankAccount bankAccount);
}
