package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "design_image")
public class DesignImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    //private String imageUrl;

    private String imageName;

    private String imagePath;

    private LocalDateTime uploadedAt;

    private Boolean isPrimary;

    @ManyToOne
    @JoinColumn(name = "design_id")
    private Design design;

    public void setImageName(String imageName) {
    }

    public void setImagePath(String imagePath) {
    }

    public void setUploadedAt(LocalDateTime now) {
    }

    public void setDesign(Design design) {
    }
}
