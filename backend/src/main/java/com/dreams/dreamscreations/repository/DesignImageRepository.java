package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignImageRepository extends JpaRepository<DesignImage, Long> {
    List<DesignImage> findByDesign(Design design);
}