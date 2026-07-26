package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinanceAccountRepository extends JpaRepository<FinanceAccount, Long> {

    Optional<FinanceAccount> findByAccountCode(String accountCode);

    boolean existsByAccountCode(String accountCode);

    List<FinanceAccount> findAllByOrderByAccountCodeAsc();

    List<FinanceAccount> findByIsActiveTrueOrderByAccountCodeAsc();

    @Query("SELECT COUNT(l) > 0 FROM FinanceJournalLine l WHERE l.account.accountId = :accountId")
    boolean hasJournalLines(@Param("accountId") Long accountId);
}
