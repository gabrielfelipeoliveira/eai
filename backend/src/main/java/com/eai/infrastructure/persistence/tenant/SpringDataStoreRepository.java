package com.eai.infrastructure.persistence.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataStoreRepository extends JpaRepository<StoreJpaEntity, UUID> {

    List<StoreJpaEntity> findByCompanyId(UUID companyId);

    List<StoreJpaEntity> findByIdIn(List<UUID> ids);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, UUID id);
}
