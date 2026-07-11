package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "module_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class ModuleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ProductionModule module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Supervisor supervisor;

    @Column(name = "quantity_sent", nullable = false)
    private Integer quantitySent;

    @Column(name = "quantity_returned_ok", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityReturnedOk = 0;

    @Column(name = "quantity_damaged", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityDamaged = 0;

    @Column(name = "quantity_missing", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityMissing = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'sent'")
    private String status = "sent";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designing_work_type_id")
    private DesigningWorkType designingWorkType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filling_work_type_id")
    private FillingWorkType fillingWorkType;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ModuleAssignmentSkuLine> skuLines = new ArrayList<>();
}
