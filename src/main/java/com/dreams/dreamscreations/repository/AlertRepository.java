package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(String status);
    List<Alert> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);
    boolean existsByRelatedEntityTypeAndRelatedEntityIdAndStatus(String entityType, Long entityId, String status);
}
