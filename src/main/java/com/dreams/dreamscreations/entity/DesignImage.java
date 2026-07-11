package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "design_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class DesignImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "design_image_id")
    private Long designImageId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_id", nullable = false)
    private Design design;

    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @Column(name = "display_order", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isPrimary = false;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
