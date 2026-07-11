package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.Size;
import com.dreams.dreamscreations.entity.Suit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SuitRepository extends JpaRepository<Suit, Long> {
    List<Suit> findByDesign(Design design);
    List<Suit> findByStatus(String status);
    Optional<Suit> findByDesignAndSizeAndColor(Design design, Size size, String color);
    Optional<Suit> findByDesignAndSizeIsNullAndColor(Design design, String color);
}
