package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinanceJournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FinanceJournalLineRepository extends JpaRepository<FinanceJournalLine, Long> {

    interface TrialBalanceProjection {
        Long getAccountId();
        String getAccountCode();
        String getAccountName();
        String getAccountType();
        java.math.BigDecimal getTotalDebit();
        java.math.BigDecimal getTotalCredit();
    }

    interface GeneralLedgerProjection {
        LocalDate getEntryDate();
        String getEntryNumber();
        String getEntryMemo();
        String getLineMemo();
        java.math.BigDecimal getDebitAmount();
        java.math.BigDecimal getCreditAmount();
        Long getEntryId();
        Integer getLineOrder();
    }

    @Query(value = """
            SELECT a.account_id AS accountId,
                   a.account_code AS accountCode,
                   a.account_name AS accountName,
                   a.account_type AS accountType,
                   COALESCE(SUM(l.debit_amount), 0) AS totalDebit,
                   COALESCE(SUM(l.credit_amount), 0) AS totalCredit
            FROM finance_account a
            LEFT JOIN finance_journal_line l ON l.account_id = a.account_id
            LEFT JOIN finance_journal_entry e ON e.entry_id = l.entry_id AND e.status = 'posted'
            WHERE (:activeOnly = 0 OR a.is_active = 1)
            GROUP BY a.account_id, a.account_code, a.account_name, a.account_type
            HAVING (:includeZero = 1 OR COALESCE(SUM(l.debit_amount), 0) > 0 OR COALESCE(SUM(l.credit_amount), 0) > 0)
            ORDER BY a.account_code
            """, nativeQuery = true)
    List<TrialBalanceProjection> trialBalance(
            @Param("activeOnly") int activeOnly,
            @Param("includeZero") int includeZero);

    @Query(value = """
            SELECT e.entry_date AS entryDate,
                   e.entry_number AS entryNumber,
                   e.memo AS entryMemo,
                   l.line_memo AS lineMemo,
                   l.debit_amount AS debitAmount,
                   l.credit_amount AS creditAmount,
                   e.entry_id AS entryId,
                   l.line_order AS lineOrder
            FROM finance_journal_line l
            INNER JOIN finance_journal_entry e ON e.entry_id = l.entry_id
            WHERE l.account_id = :accountId
              AND e.status = 'posted'
              AND e.entry_date >= :fromDate
              AND e.entry_date <= :toDate
            ORDER BY e.entry_date, e.entry_id, l.line_order
            """, nativeQuery = true)
    List<GeneralLedgerProjection> generalLedger(
            @Param("accountId") Long accountId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
