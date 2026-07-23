package com.dreams.dreamscreations.entity.finance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "finance_journal_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class FinanceJournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private FinanceJournalEntry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FinanceAccount account;

    @Column(name = "debit_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "line_memo", length = 255)
    private String lineMemo;

    @Column(name = "line_order", nullable = false)
    @Builder.Default
    private Integer lineOrder = 1;
}
