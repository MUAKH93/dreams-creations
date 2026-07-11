package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.entity.Suit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySuit(Suit suit);
    List<Product> findByStatus(String status);

    @Query("SELECT p FROM Product p JOIN FETCH p.suit s JOIN FETCH s.design " +
           "LEFT JOIN FETCH s.size sz LEFT JOIN FETCH sz.category WHERE p.productId = :id")
    Optional<Product> findByIdWithSuit(@Param("id") Long id);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.suit s JOIN FETCH s.design " +
           "LEFT JOIN FETCH s.size sz LEFT JOIN FETCH sz.category " +
           "WHERE p.status = 'active' ORDER BY s.design.designCode, sz.sizeValue, s.color")
    List<Product> findAllActiveWithSuitDetails();
}
