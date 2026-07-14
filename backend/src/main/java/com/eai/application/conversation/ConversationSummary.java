package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummary(
        UUID id,
        UUID companyId,
        UUID storeId,
        UUID contactId,
        UUID leadId,
        UUID responsibleUserId,
        String leadName,
        String phone,
        String contactDisplayName,
        UUID lastMessageId,
        ConversationMessageDirection lastMessageDirection,
        ConversationMessageType lastMessageType,
        ConversationMessageStatus lastMessageStatus,
        String lastMessageContent,
        Instant lastInteractionAt,
        long unreadCount,
        Instant createdAt,
        Instant updatedAt
) {
}
