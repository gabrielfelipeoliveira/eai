package com.eai.infrastructure.persistence.lgpd;

import com.eai.domain.lgpd.LgpdActionType;
import com.eai.domain.lgpd.LgpdRequestStatus;
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
@Table(name = "lgpd_request_actions")
public class LgpdRequestActionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "executor_user_id", nullable = false)
    private UUID executorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private LgpdActionType actionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resolution;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status")
    private LgpdRequestStatus finalStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
