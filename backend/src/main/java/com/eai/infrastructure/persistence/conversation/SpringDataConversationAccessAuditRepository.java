package com.eai.infrastructure.persistence.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataConversationAccessAuditRepository extends JpaRepository<ConversationAccessAuditJpaEntity, UUID> {
}
