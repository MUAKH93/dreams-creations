package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.EmailVerificationToken;
import com.dreams.dreamscreations.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    @Query("SELECT t FROM EmailVerificationToken t JOIN FETCH t.user WHERE t.token = :token")
    Optional<EmailVerificationToken> findByTokenWithUser(@Param("token") String token);

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.usedAt = :now WHERE t.user = :user AND t.usedAt IS NULL")
    void invalidateActiveTokensForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    void deleteByUser_UserId(Long userId);
}
