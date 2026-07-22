package com.eai.domain.lgpd;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class LgpdRequestAction {

    private final UUID id;
    private final UUID requestId;
    private final UUID executorUserId;
    private final LgpdActionType actionType;
    private final String resolution;
    private final LgpdRequestStatus finalStatus;
    private final Instant createdAt;

    public LgpdRequestAction(
            UUID id,
            UUID requestId,
            UUID executorUserId,
            LgpdActionType actionType,
            String resolution,
            LgpdRequestStatus finalStatus,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.requestId = Objects.requireNonNull(requestId);
        this.executorUserId = Objects.requireNonNull(executorUserId);
        this.actionType = Objects.requireNonNull(actionType);
        this.resolution = requireText(resolution, "resolution");
        if (finalStatus != null && finalStatus != LgpdRequestStatus.COMPLETED && finalStatus != LgpdRequestStatus.REJECTED) {
            throw new IllegalArgumentException("finalStatus must be COMPLETED or REJECTED");
        }
        this.finalStatus = finalStatus;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static LgpdRequestAction create(
            UUID requestId,
            UUID executorUserId,
            LgpdActionType actionType,
            String resolution,
            LgpdRequestStatus finalStatus
    ) {
        return new LgpdRequestAction(
                UUID.randomUUID(),
                requestId,
                executorUserId,
                actionType,
                resolution,
                finalStatus,
                Instant.now()
        );
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
