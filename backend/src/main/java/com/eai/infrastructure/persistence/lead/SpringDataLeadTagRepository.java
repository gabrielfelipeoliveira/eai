package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeadTagRepository extends JpaRepository<LeadTagJpaEntity, UUID> {

    List<LeadTagJpaEntity> findByLeadIdOrderByNameAsc(UUID leadId);

    boolean existsByLeadIdAndTagId(UUID leadId, UUID tagId);

    boolean existsByLeadIdAndType(UUID leadId, String type);
}
