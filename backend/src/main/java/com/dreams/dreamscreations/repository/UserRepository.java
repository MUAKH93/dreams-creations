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

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.role.roleName = :roleName ORDER BY u.username")
    List<User> findAllByRoleName(@Param("roleName") String roleName);

    default Optional<User> findManagerById(Long userId) {
        return findById(userId).filter(u ->
                u.getRole() != null && "MANAGER".equals(u.getRole().getRoleName()));
    }

    default boolean usernameTakenByOther(String username, Long excludeUserId) {
        return findAllByUsername(username.trim()).stream()
                .anyMatch(u -> !u.getUserId().equals(excludeUserId));
    }

    default boolean emailTakenByOther(String email, Long excludeUserId) {
        return findAllByEmail(email).stream()
                .anyMatch(u -> !u.getUserId().equals(excludeUserId));
    }
}
