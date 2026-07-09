package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMessageRepository {

    boolean existsByExternalMessageId(String externalMessageId);

    Optional<ConversationMessage> findById(UUID id);

    List<ConversationMessage> findByConversationId(UUID conversationId);

    Optional<ConversationMessage> findLatestByConversationId(UUID conversationId);

    long countByConversationIdAndDirectionAndStatus(UUID conversationId, ConversationMessageDirection direction, ConversationMessageStatus status);

    void markInboundReceivedAsRead(UUID conversationId);

    ConversationMessage save(ConversationMessage message);
}
