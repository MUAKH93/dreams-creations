package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.PasswordResetToken;
import com.dreams.dreamscreations.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT t FROM PasswordResetToken t JOIN FETCH t.user WHERE t.token = :token")
    Optional<PasswordResetToken> findByTokenWithUser(@Param("token") String token);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :usedAt WHERE t.user = :user AND t.usedAt IS NULL")
    void invalidateActiveTokensForUser(@Param("user") User user, @Param("usedAt") LocalDateTime usedAt);
}
