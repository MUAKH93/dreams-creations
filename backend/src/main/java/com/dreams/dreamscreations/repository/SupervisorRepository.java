package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {
    List<Supervisor> findByStatus(String status);

    @Query("SELECT s FROM Supervisor s WHERE LOWER(s.email) = LOWER(:email)")
    List<Supervisor> findAllByEmail(@Param("email") String email);

    default Optional<Supervisor> findFirstByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return findAllByEmail(email.trim()).stream().findFirst();
    }
}
