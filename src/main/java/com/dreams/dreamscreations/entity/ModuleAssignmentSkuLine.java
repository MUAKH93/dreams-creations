package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "module_assignment_sku_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class ModuleAssignmentSkuLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ModuleAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @Column(name = "color", nullable = false, length = 30)
    private String color;

    @Column(name = "quantity_sent", nullable = false)
    private Integer quantitySent;

    @Column(name = "quantity_returned_ok", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityReturnedOk = 0;

    @Column(name = "quantity_damaged", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityDamaged = 0;

    @Column(name = "quantity_missing", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityMissing = 0;
}
