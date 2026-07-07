package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeadHistoryRepository extends JpaRepository<LeadHistoryJpaEntity, UUID> {

    List<LeadHistoryJpaEntity> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
}
