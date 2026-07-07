package com.eai.infrastructure.persistence.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeadCommunicationRepository extends JpaRepository<LeadCommunicationJpaEntity, UUID> {

    List<LeadCommunicationJpaEntity> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
}
