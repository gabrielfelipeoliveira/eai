package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMessageRepository {

    boolean existsByExternalMessageId(String externalMessageId);

    Optional<ConversationMessage> findById(UUID id);

    List<ConversationMessage> findByConversationId(UUID conversationId);

    ConversationMessage save(ConversationMessage message);
}
