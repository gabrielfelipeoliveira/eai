package com.eai.infrastructure.persistence.conversation;

import com.eai.domain.conversation.ConversationMessageEvent;
import com.eai.domain.conversation.ConversationMessageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationMessageEventPersistenceAdapterTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    private final SpringDataConversationMessageEventRepository repository = mock(SpringDataConversationMessageEventRepository.class);
    private final ConversationMessageEventPersistenceAdapter adapter = new ConversationMessageEventPersistenceAdapter(repository);

    @DisplayName("Salva evento de mensagem convertendo dominio e entidade")
    @Test
    void savesMessageEventMappingDomainAndEntity() {
        when(repository.save(any(ConversationMessageEventJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationMessageEvent saved = adapter.save(event());

        assertThat(saved.getId()).isEqualTo(EVENT_ID);
        assertThat(saved.getMessageId()).isEqualTo(MESSAGE_ID);
        assertThat(saved.getStatus()).isEqualTo(ConversationMessageStatus.FAILED);
        verify(repository).save(any(ConversationMessageEventJpaEntity.class));
    }

    private ConversationMessageEvent event() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new ConversationMessageEvent(
                EVENT_ID,
                MESSAGE_ID,
                "wamid.1",
                ConversationMessageStatus.FAILED,
                "erro",
                "{}",
                now,
                now
        );
    }
}
