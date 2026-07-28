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
@Table(name = "finance_bank_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class FinanceBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "account_name", nullable = false, length = 120)
    private String accountName;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "account_number", length = 40)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private FinanceAccount glAccount;

    @Column(name = "opening_balance", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "opening_balance_date")
    private LocalDate openingBalanceDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
