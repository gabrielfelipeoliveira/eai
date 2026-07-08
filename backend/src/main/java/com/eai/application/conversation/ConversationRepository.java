package com.eai.application.conversation;

import com.eai.domain.conversation.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

    Optional<Conversation> findById(UUID id);

    Optional<Conversation> findByContactId(UUID contactId);

    Optional<Conversation> findByLeadId(UUID leadId);

    List<Conversation> findAll();

    List<Conversation> findByCompanyId(UUID companyId);

    List<Conversation> findByStoreId(UUID storeId);

    Conversation save(Conversation conversation);
}
