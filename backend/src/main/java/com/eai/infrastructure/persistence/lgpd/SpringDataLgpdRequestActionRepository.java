package com.eai.infrastructure.persistence.lgpd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLgpdRequestActionRepository extends JpaRepository<LgpdRequestActionJpaEntity, UUID> {

    List<LgpdRequestActionJpaEntity> findByRequestIdOrderByCreatedAtDesc(UUID requestId);
}
