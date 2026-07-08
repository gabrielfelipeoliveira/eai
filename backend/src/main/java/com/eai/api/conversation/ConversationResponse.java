package com.eai.api.conversation;

import com.eai.domain.conversation.Conversation;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID companyId,
        UUID storeId,
        UUID contactId,
        UUID leadId,
        UUID responsibleUserId,
        Instant createdAt,
        Instant updatedAt
) {

    public static ConversationResponse fromDomain(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getCompanyId(),
                conversation.getStoreId(),
                conversation.getContactId(),
                conversation.getLeadId(),
                conversation.getResponsibleUserId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
