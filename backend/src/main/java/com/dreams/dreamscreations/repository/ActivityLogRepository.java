package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a LEFT JOIN FETCH a.performedBy ORDER BY a.createdAt DESC")
    List<ActivityLog> findAllWithUser();
}
