package com.dreams.dreamscreations.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "design_image")
public class DesignImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long image_id;

    private String image_name;

    private String image_path;

    private Boolean is_primary;

    private LocalDateTime uploaded_at;

    public Long getImage_id() {
        return image_id;
    }

    public void setImage_id(Long image_id) {
        this.image_id = image_id;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public Boolean getIs_primary() {
        return is_primary;
    }

    public void setIs_primary(Boolean is_primary) {
        this.is_primary = is_primary;
    }

    public LocalDateTime getUploaded_at() {
        return uploaded_at;
    }

    public void setUploaded_at(LocalDateTime uploaded_at) {
        this.uploaded_at = uploaded_at;
    }

    public Design getDesign(){
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    @ManyToOne
    @JoinColumn(name = "design_id")
    private Design design;

    public DesignImage() {
    }
}
