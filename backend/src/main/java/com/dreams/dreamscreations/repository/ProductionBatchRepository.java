package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.Suit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
    @Query("SELECT b FROM ProductionBatch b JOIN FETCH b.suit s JOIN FETCH s.design JOIN FETCH s.size")
    List<ProductionBatch> findAllWithDetails();

    List<ProductionBatch> findBySuit(Suit suit);
    List<ProductionBatch> findByStatus(String status);
    Optional<ProductionBatch> findByBatchNumber(String batchNumber);
}
