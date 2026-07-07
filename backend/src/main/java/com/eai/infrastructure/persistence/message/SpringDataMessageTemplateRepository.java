package com.eai.infrastructure.persistence.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataMessageTemplateRepository extends JpaRepository<MessageTemplateJpaEntity, UUID> {

    List<MessageTemplateJpaEntity> findAllByOrderByNameAsc();

    List<MessageTemplateJpaEntity> findByCompanyIdOrderByNameAsc(UUID companyId);

    List<MessageTemplateJpaEntity> findByStoreIdOrderByNameAsc(UUID storeId);

    List<MessageTemplateJpaEntity> findByActiveTrueOrderByNameAsc();

    List<MessageTemplateJpaEntity> findByCompanyIdAndActiveTrueOrderByNameAsc(UUID companyId);

    List<MessageTemplateJpaEntity> findByStoreIdAndActiveTrueOrderByNameAsc(UUID storeId);
}
