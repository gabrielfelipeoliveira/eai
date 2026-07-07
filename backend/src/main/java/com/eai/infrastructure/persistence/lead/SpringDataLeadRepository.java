package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataLeadRepository extends JpaRepository<LeadJpaEntity, UUID>, JpaSpecificationExecutor<LeadJpaEntity> {

    List<LeadJpaEntity> findByStoreIdAndAssignedToUserIdIsNullAndStatusInOrderByCreatedAtAsc(UUID storeId, List<com.eai.domain.lead.LeadStatus> statuses);

    List<LeadJpaEntity> findByStoreIdAndStatusInOrderByCreatedAtAsc(UUID storeId, List<com.eai.domain.lead.LeadStatus> statuses);

    Optional<LeadJpaEntity> findTopByStoreIdAndAssignedToUserIdIsNotNullOrderByAssignedAtDescUpdatedAtDesc(UUID storeId);

    long countByAssignedToUserIdAndStatusIn(UUID userId, List<com.eai.domain.lead.LeadStatus> statuses);

    @Query("""
            select count(lead) > 0
            from LeadJpaEntity lead
            where lead.storeId = :storeId
              and lead.customerPhone = :phone
              and lower(lead.vehicleInterest) = :vehicleInterest
              and lead.createdAt >= :since
            """)
    boolean existsDuplicate(
            @Param("storeId") UUID storeId,
            @Param("phone") String phone,
            @Param("vehicleInterest") String vehicleInterest,
            @Param("since") Instant since
    );
}
