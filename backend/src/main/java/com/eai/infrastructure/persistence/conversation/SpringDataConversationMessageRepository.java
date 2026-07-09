package com.eai.infrastructure.persistence.conversation;

import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataConversationMessageRepository extends JpaRepository<ConversationMessageJpaEntity, UUID> {

    boolean existsByExternalMessageId(String externalMessageId);

    List<ConversationMessageJpaEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    Optional<ConversationMessageJpaEntity> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    long countByConversationIdAndDirectionAndStatus(UUID conversationId, ConversationMessageDirection direction, ConversationMessageStatus status);

    @Modifying
    @Query("""
            update ConversationMessageJpaEntity message
               set message.status = :readStatus,
                   message.updatedAt = :updatedAt
             where message.conversationId = :conversationId
               and message.direction = :direction
               and message.status = :receivedStatus
            """)
    int markInboundReceivedAsRead(
            @Param("conversationId") UUID conversationId,
            @Param("direction") ConversationMessageDirection direction,
            @Param("receivedStatus") ConversationMessageStatus receivedStatus,
            @Param("readStatus") ConversationMessageStatus readStatus,
            @Param("updatedAt") Instant updatedAt
    );
}
