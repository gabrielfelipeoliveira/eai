package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.conversation.ConversationMessageRepository;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.WhatsAppContactRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.conversation.WhatsAppContact;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WhatsAppTextSenderServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONVERSATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID CONTACT_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppContactRepository contactRepository = mock(WhatsAppContactRepository.class);
    private final ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
    private final WhatsAppChannelSettings settings = mock(WhatsAppChannelSettings.class);
    private final WhatsAppTextClient textClient = mock(WhatsAppTextClient.class);
    private final WhatsAppTextSenderService service = new WhatsAppTextSenderService(
            conversationService,
            contactRepository,
            messageRepository,
            settings,
            textClient
    );

    @Test
    void sendsFreeTextWhenLatestInboundMessageIsWithin24HourWindow() {
        Conversation conversation = conversation();
        when(settings.templateSendingConfigured()).thenReturn(true);
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation);
        when(messageRepository.findLatestByConversationIdAndDirection(CONVERSATION_ID, ConversationMessageDirection.INBOUND))
                .thenReturn(Optional.of(message(ConversationMessageDirection.INBOUND, ConversationMessageStatus.RECEIVED, "Oi", Instant.now().minusSeconds(3600))));
        when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact()));
        when(textClient.sendText("5511999990000", "Bom dia"))
                .thenReturn(new WhatsAppTextProviderResult(true, 200, "wamid.text-001", "{\"messages\":[{\"id\":\"wamid.text-001\"}]}"));
        when(messageRepository.save(any(ConversationMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WhatsAppTextSendResult result = service.sendText(CONVERSATION_ID, " Bom dia ", authenticatedUser());

        assertThat(result.status()).isEqualTo(ConversationMessageStatus.SENT);
        assertThat(result.externalMessageId()).isEqualTo("wamid.text-001");
        assertThat(result.message()).isEqualTo("Bom dia");
        verify(textClient).sendText("5511999990000", "Bom dia");
    }

    @Test
    void blocksFreeTextWhenLatestInboundMessageIsOutside24HourWindow() {
        when(settings.templateSendingConfigured()).thenReturn(true);
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation());
        when(messageRepository.findLatestByConversationIdAndDirection(CONVERSATION_ID, ConversationMessageDirection.INBOUND))
                .thenReturn(Optional.of(message(ConversationMessageDirection.INBOUND, ConversationMessageStatus.RECEIVED, "Oi", Instant.now().minusSeconds(25 * 60 * 60))));

        assertThatThrownBy(() -> service.sendText(CONVERSATION_ID, "Bom dia", authenticatedUser()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Use a WhatsApp template");

        verify(textClient, never()).sendText(any(), any());
        verify(messageRepository, never()).save(any());
    }

    private Conversation conversation() {
        return new Conversation(
                CONVERSATION_ID,
                COMPANY_ID,
                STORE_ID,
                CONTACT_ID,
                null,
                USER_ID,
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600)
        );
    }

    private WhatsAppContact contact() {
        return new WhatsAppContact(CONTACT_ID, COMPANY_ID, STORE_ID, null, "11999990000", "Cliente", Instant.now(), Instant.now());
    }

    private ConversationMessage message(ConversationMessageDirection direction, ConversationMessageStatus status, String content, Instant createdAt) {
        return new ConversationMessage(
                UUID.randomUUID(),
                CONVERSATION_ID,
                direction,
                ConversationMessageType.TEXT,
                status,
                null,
                content,
                null,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(USER_ID, "admin@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.ADMIN));
    }
}
