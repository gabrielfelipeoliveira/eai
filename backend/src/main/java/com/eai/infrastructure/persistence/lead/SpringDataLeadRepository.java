package com.eai.infrastructure.persistence.lead;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.UUID;

public interface SpringDataLeadRepository extends JpaRepository<LeadJpaEntity, UUID>, JpaSpecificationExecutor<LeadJpaEntity> {

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
