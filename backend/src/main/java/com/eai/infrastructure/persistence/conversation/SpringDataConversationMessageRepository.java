package com.eai.infrastructure.persistence.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataConversationMessageRepository extends JpaRepository<ConversationMessageJpaEntity, UUID> {

    boolean existsByExternalMessageId(String externalMessageId);

    List<ConversationMessageJpaEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
