package com.eai.domain.notification;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Notification {

    private final UUID id;
    private final UUID recipientUserId;
    private final NotificationType type;
    private final NotificationSeverity severity;
    private final String title;
    private final String message;
    private final String relatedEntityType;
    private final UUID relatedEntityId;
    private NotificationExternalDeliveryStatus externalDeliveryStatus;
    private Instant readAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public Notification(
            UUID id,
            UUID recipientUserId,
            NotificationType type,
            NotificationSeverity severity,
            String title,
            String message,
            String relatedEntityType,
            UUID relatedEntityId,
            NotificationExternalDeliveryStatus externalDeliveryStatus,
            Instant readAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.recipientUserId = Objects.requireNonNull(recipientUserId);
        this.type = Objects.requireNonNull(type);
        this.severity = Objects.requireNonNull(severity);
        this.title = requireText(title, "title");
        this.message = requireText(message, "message");
        this.relatedEntityType = trimToNull(relatedEntityType);
        this.relatedEntityId = relatedEntityId;
        this.externalDeliveryStatus = externalDeliveryStatus == null
                ? NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY
                : externalDeliveryStatus;
        this.readAt = readAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Notification create(
            UUID recipientUserId,
            NotificationType type,
            NotificationSeverity severity,
            String title,
            String message,
            String relatedEntityType,
            UUID relatedEntityId
    ) {
        Instant now = Instant.now();
        return new Notification(
                UUID.randomUUID(),
                recipientUserId,
                type,
                severity,
                title,
                message,
                relatedEntityType,
                relatedEntityId,
                NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY,
                null,
                now,
                now
        );
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markRead() {
        if (readAt != null) {
            return;
        }
        Instant now = Instant.now();
        this.readAt = now;
        this.updatedAt = now;
    }

    public void updateExternalDeliveryStatus(NotificationExternalDeliveryStatus status) {
        this.externalDeliveryStatus = Objects.requireNonNull(status);
        this.updatedAt = Instant.now();
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
