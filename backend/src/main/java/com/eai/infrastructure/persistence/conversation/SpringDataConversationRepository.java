package com.eai.infrastructure.persistence.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataConversationRepository extends JpaRepository<ConversationJpaEntity, UUID> {

    Optional<ConversationJpaEntity> findByContactId(UUID contactId);

    Optional<ConversationJpaEntity> findByLeadId(UUID leadId);

    List<ConversationJpaEntity> findByCompanyIdOrderByUpdatedAtDesc(UUID companyId);

    List<ConversationJpaEntity> findByStoreIdOrderByUpdatedAtDesc(UUID storeId);

    List<ConversationJpaEntity> findByResponsibleUserIdOrderByUpdatedAtDesc(UUID responsibleUserId);

    List<ConversationJpaEntity> findAllByOrderByUpdatedAtDesc();
}
