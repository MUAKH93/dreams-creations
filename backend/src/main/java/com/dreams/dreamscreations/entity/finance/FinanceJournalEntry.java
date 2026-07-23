package com.dreams.dreamscreations.entity.finance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "finance_journal_entry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class FinanceJournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "entry_number", nullable = false, unique = true, length = 30)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_period_id")
    private FinanceFiscalPeriod fiscalPeriod;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "source_type", nullable = false, length = 30)
    @Builder.Default
    private String sourceType = "manual";

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "posted";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FinanceJournalLine> lines = new ArrayList<>();
}
