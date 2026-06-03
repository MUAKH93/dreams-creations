package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "embroidery_type")
public class EmbroideryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long embroideryTypeId;

    private String name;

    private String description;
}
