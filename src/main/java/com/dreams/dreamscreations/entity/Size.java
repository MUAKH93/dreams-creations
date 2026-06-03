package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "size")
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sizeId;

    private String sizeValue;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}
