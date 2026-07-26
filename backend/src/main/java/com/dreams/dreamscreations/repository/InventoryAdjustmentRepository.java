package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {

    @Query("SELECT a FROM InventoryAdjustment a " +
           "JOIN FETCH a.suit s JOIN FETCH s.design " +
           "LEFT JOIN FETCH s.size " +
           "LEFT JOIN FETCH a.adjustedBy " +
           "ORDER BY a.adjustmentId DESC")
    List<InventoryAdjustment> findAllWithDetails();
}
