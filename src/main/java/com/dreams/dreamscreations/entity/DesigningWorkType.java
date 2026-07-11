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
@Table(name = "designing_work_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class DesigningWorkType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designing_work_type_id")
    private Long designingWorkTypeId;

    @Column(name = "type_name", nullable = false, unique = true, length = 80)
    private String typeName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'active'")
    private String status = "active";
}
