package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataLeadRepository extends JpaRepository<LeadJpaEntity, UUID>, JpaSpecificationExecutor<LeadJpaEntity> {

    List<LeadJpaEntity> findByStoreIdAndAssignedToUserIdIsNullAndStatusInOrderByCreatedAtAsc(UUID storeId, List<com.eai.domain.lead.LeadStatus> statuses);

    List<LeadJpaEntity> findByStoreIdAndStatusInOrderByCreatedAtAsc(UUID storeId, List<com.eai.domain.lead.LeadStatus> statuses);

    Optional<LeadJpaEntity> findTopByStoreIdAndAssignedToUserIdIsNotNullOrderByAssignedAtDescUpdatedAtDesc(UUID storeId);

    long countByAssignedToUserIdAndStatusIn(UUID userId, List<com.eai.domain.lead.LeadStatus> statuses);

    @Query("""
            select distinct lead
            from LeadJpaEntity lead
            left join lead.additionalPhones additionalPhone
            where lead.storeId = :storeId
              and (
                    lead.customerPhone in :phones
                    or additionalPhone in :phones
              )
            order by lead.createdAt desc
            """)
    List<LeadJpaEntity> findByStoreIdAndAnyPhoneOrderByCreatedAtDesc(
            @Param("storeId") UUID storeId,
            @Param("phones") List<String> phones
    );
}
