package com.dreams.dreamscreations.repository.finance;

import com.dreams.dreamscreations.entity.finance.FinancePostingLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancePostingLinkRepository extends JpaRepository<FinancePostingLink, Long> {

    Optional<FinancePostingLink> findByEntityTypeAndEntityId(String entityType, Long entityId);

    boolean existsByEntityTypeAndEntityId(String entityType, Long entityId);
}
