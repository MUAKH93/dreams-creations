package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "design_required_stage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class DesignRequiredStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "design_stage_id")
    private Long designStageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_id", nullable = false)
    private Design design;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private ProductionStage stage;

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @Column(name = "is_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isRequired = true;
}
