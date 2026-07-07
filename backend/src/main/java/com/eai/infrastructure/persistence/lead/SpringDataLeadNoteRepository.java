package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeadNoteRepository extends JpaRepository<LeadNoteJpaEntity, UUID> {

    List<LeadNoteJpaEntity> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
}
