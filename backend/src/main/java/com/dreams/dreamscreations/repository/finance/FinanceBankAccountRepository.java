package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinanceBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FinanceBankAccountRepository extends JpaRepository<FinanceBankAccount, Long> {

    @Query("SELECT b FROM FinanceBankAccount b JOIN FETCH b.glAccount ORDER BY b.accountName")
    List<FinanceBankAccount> findAllWithGlAccount();

    @Query("SELECT b FROM FinanceBankAccount b JOIN FETCH b.glAccount WHERE b.bankAccountId = :id")
    Optional<FinanceBankAccount> findByIdWithGlAccount(Long id);

    List<FinanceBankAccount> findByIsActiveTrueOrderByAccountNameAsc();
}
