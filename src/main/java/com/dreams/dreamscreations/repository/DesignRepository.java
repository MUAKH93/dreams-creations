package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Design;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DesignRepository extends JpaRepository<Design, Long> {

    @Query("SELECT DISTINCT d FROM Design d " +
           "LEFT JOIN FETCH d.images " +
           "LEFT JOIN FETCH d.category " +
           "LEFT JOIN FETCH d.designType")
    List<Design> findAllWithImages();

    @Query("SELECT d FROM Design d LEFT JOIN FETCH d.images WHERE d.designId = :id")
    Optional<Design> findByIdWithImages(@Param("id") Long id);
}
