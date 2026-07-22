package com.eai.infrastructure.persistence.lgpd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SpringDataLgpdRequestRepository extends JpaRepository<LgpdRequestJpaEntity, UUID>, JpaSpecificationExecutor<LgpdRequestJpaEntity> {
}
