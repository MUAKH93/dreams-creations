package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import java.time.LocalDate;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "production_batch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suit_id", nullable = false)
    private Suit suit;

    @Column(name = "batch_number", nullable = false, unique = true, length = 30)
    private String batchNumber;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'planned'")
    private String status = "planned";

    @Column(name = "total_suit_planned", nullable = false)
    private Integer totalSuitPlanned;

    @Column(name = "total_suit_produced", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer totalSuitProduced = 0;
}
