package com.eai.application.notification;

import com.eai.domain.notification.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findByRecipient(UUID recipientUserId, boolean unreadOnly, int limit);

    long countUnreadByRecipient(UUID recipientUserId);

    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    List<Notification> findUnreadByRecipient(UUID recipientUserId);
}
