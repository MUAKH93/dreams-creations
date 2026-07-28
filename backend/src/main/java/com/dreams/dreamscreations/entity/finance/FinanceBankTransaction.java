package com.dreams.dreamscreations.entity.finance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "finance_bank_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class FinanceBankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private FinanceBankAccount bankAccount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_no", length = 60)
    private String referenceNo;

    @Column(name = "is_reconciled", nullable = false)
    @Builder.Default
    private Boolean isReconciled = false;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_entry_id")
    private FinanceJournalEntry matchedEntry;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
