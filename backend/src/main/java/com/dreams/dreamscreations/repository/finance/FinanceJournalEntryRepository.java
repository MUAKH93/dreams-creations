package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinanceJournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinanceJournalEntryRepository extends JpaRepository<FinanceJournalEntry, Long> {

    @Query("SELECT DISTINCT e FROM FinanceJournalEntry e "
           + "LEFT JOIN FETCH e.lines l LEFT JOIN FETCH l.account "
           + "ORDER BY e.entryDate DESC, e.entryId DESC")
    List<FinanceJournalEntry> findAllWithLines();

    @Query("SELECT DISTINCT e FROM FinanceJournalEntry e "
           + "LEFT JOIN FETCH e.lines l LEFT JOIN FETCH l.account "
           + "WHERE e.entryId = :id")
    Optional<FinanceJournalEntry> findByIdWithLines(@Param("id") Long id);

    Optional<FinanceJournalEntry> findTopByEntryNumberStartingWithOrderByEntryNumberDesc(String prefix);
}
