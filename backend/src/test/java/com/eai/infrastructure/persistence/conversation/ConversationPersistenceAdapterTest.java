package com.eai.infrastructure.persistence.conversation;

import com.eai.domain.conversation.Conversation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationPersistenceAdapterTest {

    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID CONTACT_ID = UUID.randomUUID();
    private static final UUID LEAD_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private final SpringDataConversationRepository repository = mock(SpringDataConversationRepository.class);
    private final ConversationPersistenceAdapter adapter = new ConversationPersistenceAdapter(repository);

    @DisplayName("Consulta conversas convertendo entidades JPA para dominio")
    @Test
    void findsConversationsMappingEntitiesToDomain() {
        ConversationJpaEntity entity = entity();
        when(repository.findById(CONVERSATION_ID)).thenReturn(Optional.of(entity));
        when(repository.findByContactId(CONTACT_ID)).thenReturn(Optional.of(entity));
        when(repository.findByLeadId(LEAD_ID)).thenReturn(Optional.of(entity));
        when(repository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(entity));
        when(repository.findByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID)).thenReturn(List.of(entity));
        when(repository.findByStoreIdOrderByUpdatedAtDesc(STORE_ID)).thenReturn(List.of(entity));
        when(repository.findByResponsibleUserIdOrderByUpdatedAtDesc(USER_ID)).thenReturn(List.of(entity));

        assertThat(adapter.findById(CONVERSATION_ID)).isPresent();
        assertThat(adapter.findByContactId(CONTACT_ID)).isPresent();
        assertThat(adapter.findByLeadId(LEAD_ID)).isPresent();
        assertThat(adapter.findAll()).hasSize(1);
        assertThat(adapter.findByCompanyId(COMPANY_ID)).hasSize(1);
        assertThat(adapter.findByStoreId(STORE_ID)).hasSize(1);
        assertThat(adapter.findByResponsibleUserId(USER_ID)).hasSize(1);
    }

    @DisplayName("Salva conversa convertendo dominio para entidade")
    @Test
    void savesConversationMappingDomainToEntity() {
        when(repository.save(any(ConversationJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Conversation saved = adapter.save(conversation());

        assertThat(saved.getId()).isEqualTo(CONVERSATION_ID);
        verify(repository).save(any(ConversationJpaEntity.class));
    }

    private Conversation conversation() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Conversation(CONVERSATION_ID, COMPANY_ID, STORE_ID, CONTACT_ID, LEAD_ID, USER_ID, now, now);
    }

    private ConversationJpaEntity entity() {
        Conversation conversation = conversation();
        ConversationJpaEntity entity = new ConversationJpaEntity();
        entity.setId(conversation.getId());
        entity.setCompanyId(conversation.getCompanyId());
        entity.setStoreId(conversation.getStoreId());
        entity.setContactId(conversation.getContactId());
        entity.setLeadId(conversation.getLeadId());
        entity.setResponsibleUserId(conversation.getResponsibleUserId());
        entity.setCreatedAt(conversation.getCreatedAt());
        entity.setUpdatedAt(conversation.getUpdatedAt());
        return entity;
    }
}
