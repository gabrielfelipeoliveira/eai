package com.eai.infrastructure.persistence.lgpd;

import com.eai.domain.lgpd.LgpdRequestStatus;
import com.eai.domain.lgpd.LgpdRequestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lgpd_requests")
public class LgpdRequestJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "lead_id")
    private UUID leadId;

    @Column(name = "data_subject_name", nullable = false)
    private String dataSubjectName;

    @Column(name = "data_subject_phone")
    private String dataSubjectPhone;

    @Column(name = "data_subject_email")
    private String dataSubjectEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private LgpdRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LgpdRequestStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requested_by_user_id", nullable = false)
    private UUID requestedByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
