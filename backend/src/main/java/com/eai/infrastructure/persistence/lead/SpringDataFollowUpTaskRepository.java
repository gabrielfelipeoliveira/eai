package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataFollowUpTaskRepository extends JpaRepository<FollowUpTaskJpaEntity, UUID> {

    List<FollowUpTaskJpaEntity> findByLeadIdOrderByDueAtAsc(UUID leadId);

    @Query("""
            select task
            from FollowUpTaskJpaEntity task
            join LeadJpaEntity lead on lead.id = task.leadId
            where (:companyId is null or lead.companyId = :companyId)
              and (:storeId is null or lead.storeId = :storeId)
              and (:userId is null or task.userId = :userId)
            order by task.dueAt asc
            """)
    List<FollowUpTaskJpaEntity> findVisible(
            @Param("companyId") UUID companyId,
            @Param("storeId") UUID storeId,
            @Param("userId") UUID userId
    );
}
