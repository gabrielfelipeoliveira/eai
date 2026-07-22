package com.eai.domain.lgpd;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class LgpdRequest {

    private final UUID id;
    private final UUID companyId;
    private final UUID storeId;
    private final UUID leadId;
    private final String dataSubjectName;
    private final String dataSubjectPhone;
    private final String dataSubjectEmail;
    private final LgpdRequestType requestType;
    private LgpdRequestStatus status;
    private final String description;
    private final UUID requestedByUserId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public LgpdRequest(
            UUID id,
            UUID companyId,
            UUID storeId,
            UUID leadId,
            String dataSubjectName,
            String dataSubjectPhone,
            String dataSubjectEmail,
            LgpdRequestType requestType,
            LgpdRequestStatus status,
            String description,
            UUID requestedByUserId,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = storeId;
        this.leadId = leadId;
        this.dataSubjectName = requireText(dataSubjectName, "dataSubjectName");
        this.dataSubjectPhone = trimToNull(dataSubjectPhone);
        this.dataSubjectEmail = trimToNull(dataSubjectEmail);
        this.requestType = Objects.requireNonNull(requestType);
        this.status = Objects.requireNonNull(status);
        this.description = requireText(description, "description");
        this.requestedByUserId = Objects.requireNonNull(requestedByUserId);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.completedAt = completedAt;
    }

    public static LgpdRequest create(
            UUID companyId,
            UUID storeId,
            UUID leadId,
            String dataSubjectName,
            String dataSubjectPhone,
            String dataSubjectEmail,
            LgpdRequestType requestType,
            String description,
            UUID requestedByUserId
    ) {
        Instant now = Instant.now();
        return new LgpdRequest(
                UUID.randomUUID(),
                companyId,
                storeId,
                leadId,
                dataSubjectName,
                dataSubjectPhone,
                dataSubjectEmail,
                requestType,
                LgpdRequestStatus.OPEN,
                description,
                requestedByUserId,
                now,
                now,
                null
        );
    }

    public void registerAction(LgpdRequestStatus finalStatus) {
        if (status == LgpdRequestStatus.COMPLETED || status == LgpdRequestStatus.REJECTED) {
            throw new IllegalArgumentException("LGPD request is already closed");
        }
        Instant now = Instant.now();
        if (finalStatus == null) {
            this.status = LgpdRequestStatus.IN_PROGRESS;
            this.completedAt = null;
        } else if (finalStatus == LgpdRequestStatus.COMPLETED || finalStatus == LgpdRequestStatus.REJECTED) {
            this.status = finalStatus;
            this.completedAt = now;
        } else {
            throw new IllegalArgumentException("finalStatus must be COMPLETED or REJECTED");
        }
        this.updatedAt = now;
    }

    private static String requireText(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
