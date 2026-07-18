package com.eai.domain.lead;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class FollowUpTask {

    private final UUID id;
    private final UUID leadId;
    private UUID userId;
    private String title;
    private String description;
    private Instant dueAt;
    private Instant completedAt;
    private FollowUpTaskStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public FollowUpTask(
            UUID id,
            UUID leadId,
            UUID userId,
            String title,
            String description,
            Instant dueAt,
            Instant completedAt,
            FollowUpTaskStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.userId = Objects.requireNonNull(userId);
        this.title = requireText(title, "title");
        this.description = trimToNull(description);
        this.dueAt = Objects.requireNonNull(dueAt);
        this.completedAt = completedAt;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static FollowUpTask create(UUID leadId, UUID userId, String title, String description, Instant dueAt) {
        Instant now = Instant.now();
        return new FollowUpTask(
                UUID.randomUUID(),
                leadId,
                userId,
                title,
                description,
                dueAt,
                null,
                FollowUpTaskStatus.PENDING,
                now,
                now
        );
    }

    public void complete() {
        if (status == FollowUpTaskStatus.CANCELED) {
            throw new IllegalStateException("Canceled follow-up cannot be completed");
        }
        Instant now = Instant.now();
        this.status = FollowUpTaskStatus.DONE;
        this.completedAt = now;
        this.updatedAt = now;
    }

    public void cancel() {
        if (status == FollowUpTaskStatus.DONE) {
            throw new IllegalStateException("Completed follow-up cannot be canceled");
        }
        Instant now = Instant.now();
        this.status = FollowUpTaskStatus.CANCELED;
        this.updatedAt = now;
    }

    public FollowUpTaskStatus effectiveStatus(Instant now) {
        if (status == FollowUpTaskStatus.PENDING && dueAt.isBefore(now)) {
            return FollowUpTaskStatus.OVERDUE;
        }
        return status;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
