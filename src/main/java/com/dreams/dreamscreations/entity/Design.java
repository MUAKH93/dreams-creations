package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "design")
public class Design {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long design_id;

    private String design_code;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name="design_type_id")
    private DesignType designType;

    @ManyToOne
    @JoinColumn(name = "embroidery_type_id")
    private EmbroideryType embroideryType;
}
