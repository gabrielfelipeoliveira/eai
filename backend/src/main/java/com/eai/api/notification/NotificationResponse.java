package com.eai.api.notification;

import com.eai.domain.notification.Notification;
import com.eai.domain.notification.NotificationExternalDeliveryStatus;
import com.eai.domain.notification.NotificationSeverity;
import com.eai.domain.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientUserId,
        NotificationType type,
        NotificationSeverity severity,
        String title,
        String message,
        String relatedEntityType,
        UUID relatedEntityId,
        NotificationExternalDeliveryStatus externalDeliveryStatus,
        boolean read,
        Instant readAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static NotificationResponse fromDomain(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getType(),
                notification.getSeverity(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getExternalDeliveryStatus(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
