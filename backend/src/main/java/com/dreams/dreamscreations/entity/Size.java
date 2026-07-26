package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Size values are scoped per category:
 * Ladies -> S, M, L, XL
 * Kids   -> 2Y, 4Y, 6Y, 8Y
 * The unique constraint on (size_value, category_id) prevents
 * duplicates within the same category.
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "size",
        uniqueConstraints = @UniqueConstraint(columnNames = {"size_value", "category_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_id")
    private Long sizeId;

    @Column(name = "size_value", nullable = false, length = 20)
    private String sizeValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "description", length = 255)
    private String description;
}
