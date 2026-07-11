package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByUsername(String username);

    default Optional<User> findFirstByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        return findAllByUsername(username.trim()).stream().findFirst();
    }

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    java.util.List<User> findAllByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.email) = LOWER(:email)")
    java.util.List<User> findAllByEmailWithRole(@Param("email") String email);

    default Optional<User> findSupervisorUserByEmail(String email) {
        return findAllByEmailWithRole(email).stream()
                .filter(u -> u.getRole() != null && "SUPERVISOR".equals(u.getRole().getRoleName()))
                .findFirst();
    }

    default boolean emailTaken(String email) {
        return !findAllByEmail(email).isEmpty();
    }

    default boolean usernameTaken(String username) {
        return !findAllByUsername(username).isEmpty();
    }
}
