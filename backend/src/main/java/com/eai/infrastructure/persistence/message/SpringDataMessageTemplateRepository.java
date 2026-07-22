package com.eai.infrastructure.persistence.message;

import com.eai.domain.message.MessageTemplateMetaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataMessageTemplateRepository extends JpaRepository<MessageTemplateJpaEntity, UUID> {

    List<MessageTemplateJpaEntity> findByDeletedAtIsNullOrderByNameAsc();

    List<MessageTemplateJpaEntity> findByCompanyIdAndDeletedAtIsNullOrderByNameAsc(UUID companyId);

    List<MessageTemplateJpaEntity> findByActiveTrueAndMetaStatusAndDeletedAtIsNullOrderByNameAsc(MessageTemplateMetaStatus metaStatus);

    List<MessageTemplateJpaEntity> findByCompanyIdAndActiveTrueAndMetaStatusAndDeletedAtIsNullOrderByNameAsc(UUID companyId, MessageTemplateMetaStatus metaStatus);

    @Query("""
            select template
              from MessageTemplateJpaEntity template
             where template.deletedAt is null
               and (template.storeId = :storeId or (template.companyId = :companyId and template.storeId is null))
             order by template.name asc
            """)
    List<MessageTemplateJpaEntity> findByStoreScopeOrderByNameAsc(@Param("companyId") UUID companyId, @Param("storeId") UUID storeId);

    @Query("""
            select template
              from MessageTemplateJpaEntity template
             where template.deletedAt is null
               and template.active = true
               and template.metaStatus = :metaStatus
               and (template.storeId = :storeId or (template.companyId = :companyId and template.storeId is null))
             order by template.name asc
            """)
    List<MessageTemplateJpaEntity> findActiveByStoreScopeOrderByNameAsc(
            @Param("companyId") UUID companyId,
            @Param("storeId") UUID storeId,
            @Param("metaStatus") MessageTemplateMetaStatus metaStatus
    );
}
