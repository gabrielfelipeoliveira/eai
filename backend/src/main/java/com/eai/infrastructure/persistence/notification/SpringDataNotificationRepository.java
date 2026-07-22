package com.eai.infrastructure.persistence.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    List<NotificationJpaEntity> findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    List<NotificationJpaEntity> findByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);

    long countByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);

    Optional<NotificationJpaEntity> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
