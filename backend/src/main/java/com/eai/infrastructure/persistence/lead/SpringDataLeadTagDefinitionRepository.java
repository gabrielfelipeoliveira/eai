package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataLeadTagDefinitionRepository extends JpaRepository<LeadTagDefinitionJpaEntity, UUID> {

    List<LeadTagDefinitionJpaEntity> findByActiveTrueOrderByTypeAscNameAsc();

    Optional<LeadTagDefinitionJpaEntity> findByActiveTrueAndNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
