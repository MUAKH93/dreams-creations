package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DesignImageRepository extends JpaRepository<DesignImage, Long> {
    List<DesignImage> findByDesign(Design design);

    Optional<DesignImage> findByImageName(String imageName);

    @Query("SELECT di FROM DesignImage di JOIN FETCH di.design d WHERE d.designId IN :designIds")
    List<DesignImage> findByDesign_DesignIdIn(@Param("designIds") Collection<Long> designIds);
}