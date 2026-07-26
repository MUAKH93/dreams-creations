package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByRoleName(String roleName);

    default Optional<Role> findFirstByRoleName(String roleName) {
        return findAllByRoleName(roleName).stream().findFirst();
    }
}
