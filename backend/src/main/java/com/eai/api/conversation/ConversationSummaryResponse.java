package com.eai.api.conversation;

import com.eai.application.conversation.ConversationSummary;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummaryResponse(
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

    public static ConversationSummaryResponse fromApplication(ConversationSummary summary) {
        return new ConversationSummaryResponse(
                summary.id(),
                summary.companyId(),
                summary.storeId(),
                summary.contactId(),
                summary.leadId(),
                summary.responsibleUserId(),
                summary.leadName(),
                summary.phone(),
                summary.contactDisplayName(),
                summary.lastMessageId(),
                summary.lastMessageDirection(),
                summary.lastMessageType(),
                summary.lastMessageStatus(),
                summary.lastMessageContent(),
                summary.lastInteractionAt(),
                summary.unreadCount(),
                summary.createdAt(),
                summary.updatedAt()
        );
    }
}
