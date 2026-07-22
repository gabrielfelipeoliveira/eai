package com.eai.application.notification;

import com.eai.domain.notification.NotificationSeverity;
import com.eai.domain.notification.NotificationType;

import java.util.UUID;

public record CreateNotificationCommand(
        UUID recipientUserId,
        NotificationType type,
        NotificationSeverity severity,
        String title,
        String message,
        String relatedEntityType,
        UUID relatedEntityId
) {
}
