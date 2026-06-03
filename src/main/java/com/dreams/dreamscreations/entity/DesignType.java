package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "design_type")
public class DesignType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long designType_id;

    private String typeName;

    private String description;
}
