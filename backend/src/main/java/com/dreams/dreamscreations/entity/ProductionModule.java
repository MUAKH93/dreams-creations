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
@Table(name = "production_module")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class ProductionModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private ProductionStage stage;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'active'")
    private String status = "active";

    @Column(name = "description", length = 255)
    private String description;
}
