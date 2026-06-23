package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "design")
public class Design {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long design_id;

    private String description;
    private String design_code;
    private String name;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDesign_code() {
        return design_code;
    }

    public void setDesign_code(String design_code) {
        this.design_code = design_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


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
