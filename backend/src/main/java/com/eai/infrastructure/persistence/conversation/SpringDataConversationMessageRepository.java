package com.eai.infrastructure.persistence.conversation;

import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataConversationMessageRepository extends JpaRepository<ConversationMessageJpaEntity, UUID> {

    boolean existsByExternalMessageId(String externalMessageId);

    List<ConversationMessageJpaEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    Optional<ConversationMessageJpaEntity> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    long countByConversationIdAndDirectionAndStatus(UUID conversationId, ConversationMessageDirection direction, ConversationMessageStatus status);
}
