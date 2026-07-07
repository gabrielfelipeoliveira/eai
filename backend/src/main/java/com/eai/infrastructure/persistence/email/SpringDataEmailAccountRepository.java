package com.eai.infrastructure.persistence.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataEmailAccountRepository extends JpaRepository<EmailAccountJpaEntity, UUID> {

    List<EmailAccountJpaEntity> findByActiveTrue();

    List<EmailAccountJpaEntity> findByCompanyId(UUID companyId);

    List<EmailAccountJpaEntity> findByStoreIdIn(List<UUID> storeIds);
}
