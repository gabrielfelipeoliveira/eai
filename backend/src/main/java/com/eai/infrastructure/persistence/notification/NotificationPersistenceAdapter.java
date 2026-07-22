package com.eai.infrastructure.persistence.notification;

import com.eai.application.notification.NotificationRepository;
import com.eai.domain.notification.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository repository;

    @Override
    public Notification save(Notification notification) {
        return toDomain(repository.save(toEntity(notification)));
    }

    @Override
    public List<Notification> findByRecipient(UUID recipientUserId, boolean unreadOnly, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<NotificationJpaEntity> notifications = unreadOnly
                ? repository.findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(recipientUserId, pageRequest)
                : repository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId, pageRequest);
        return notifications.stream().map(this::toDomain).toList();
    }

    @Override
    public long countUnreadByRecipient(UUID recipientUserId) {
        return repository.countByRecipientUserIdAndReadAtIsNull(recipientUserId);
    }

    @Override
    public Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId) {
        return repository.findByIdAndRecipientUserId(id, recipientUserId).map(this::toDomain);
    }

    @Override
    public List<Notification> findUnreadByRecipient(UUID recipientUserId) {
        return repository.findByRecipientUserIdAndReadAtIsNull(recipientUserId).stream()
                .map(this::toDomain)
                .toList();
    }

    private Notification toDomain(NotificationJpaEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getType(),
                entity.getSeverity(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getRelatedEntityType(),
                entity.getRelatedEntityId(),
                entity.getExternalDeliveryStatus(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setRecipientUserId(notification.getRecipientUserId());
        entity.setType(notification.getType());
        entity.setSeverity(notification.getSeverity());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setRelatedEntityType(notification.getRelatedEntityType());
        entity.setRelatedEntityId(notification.getRelatedEntityId());
        entity.setExternalDeliveryStatus(notification.getExternalDeliveryStatus());
        entity.setReadAt(notification.getReadAt());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setUpdatedAt(notification.getUpdatedAt());
        return entity;
    }
}
