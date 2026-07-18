package com.eai.domain.conversation;

import lombok.Getter;

import com.eai.domain.user.UserRole;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ConversationAccessAudit {

    private final UUID id;
    private final UUID conversationId;
    private final UUID companyId;
    private final UUID storeId;
    private final UUID leadId;
    private final UUID actorUserId;
    private final UserRole actorRole;
    private final String accessType;
    private final Instant accessedAt;

    public ConversationAccessAudit(UUID id, UUID conversationId, UUID companyId, UUID storeId, UUID leadId, UUID actorUserId, UserRole actorRole, String accessType, Instant accessedAt) {
        this.id = Objects.requireNonNull(id);
        this.conversationId = Objects.requireNonNull(conversationId);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.leadId = leadId;
        this.actorUserId = Objects.requireNonNull(actorUserId);
        this.actorRole = Objects.requireNonNull(actorRole);
        this.accessType = Objects.requireNonNull(accessType);
        this.accessedAt = Objects.requireNonNull(accessedAt);
    }

    public static ConversationAccessAudit record(Conversation conversation, UUID actorUserId, UserRole actorRole, String accessType) {
        return new ConversationAccessAudit(
                UUID.randomUUID(),
                conversation.getId(),
                conversation.getCompanyId(),
                conversation.getStoreId(),
                conversation.getLeadId(),
                actorUserId,
                actorRole,
                accessType,
                Instant.now()
        );
    }

}
