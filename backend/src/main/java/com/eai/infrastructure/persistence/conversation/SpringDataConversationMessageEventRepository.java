package com.eai.infrastructure.persistence.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataConversationMessageEventRepository extends JpaRepository<ConversationMessageEventJpaEntity, UUID> {
}
