package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "design")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class Design {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "design_id")
    private Long designId;

    @Column(name = "design_code", nullable = false, unique = true, length = 30)
    private String designCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Optional reference / catalog price for this design (Rs.) */
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Estimated production cost per suit (Rs.) — for profitability reports */
    @Column(name = "production_cost", precision = 10, scale = 2)
    private BigDecimal productionCost;

    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isFeatured = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_type_id", nullable = false)
    private DesignType designType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embroidery_type_id")
    private EmbroideryType embroideryType;

    @OneToMany(mappedBy = "design", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DesignImage> images;
}
