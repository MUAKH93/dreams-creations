package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Inventory;
import com.dreams.dreamscreations.entity.Suit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySuit(Suit suit);

    @Query("SELECT i FROM Inventory i " +
           "JOIN FETCH i.suit s JOIN FETCH s.design " +
           "LEFT JOIN FETCH s.size sz LEFT JOIN FETCH sz.category " +
           "ORDER BY i.lastUpdated DESC")
    List<Inventory> findAllWithDetails();
}
