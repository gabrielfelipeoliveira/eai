package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationAccessAudit;

public interface ConversationAccessAuditRepository {

    ConversationAccessAudit save(ConversationAccessAudit audit);
}
