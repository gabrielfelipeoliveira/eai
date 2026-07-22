package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.conversation.ConversationMessageRepository;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.WhatsAppContactRepository;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.media.StoreMediaCommand;
import com.eai.application.media.StoredMedia;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.conversation.WhatsAppContact;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WhatsAppMediaSenderServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONVERSATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID CONTACT_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppContactRepository contactRepository = mock(WhatsAppContactRepository.class);
    private final ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
    private final WhatsAppChannelSettings settings = mock(WhatsAppChannelSettings.class);
    private final WhatsAppMediaClient mediaClient = mock(WhatsAppMediaClient.class);
    private final MediaStoragePort mediaStorage = mock(MediaStoragePort.class);
    private final WhatsAppMediaSettings mediaSettings = mock(WhatsAppMediaSettings.class);
    private final WhatsAppMediaValidator mediaValidator = new WhatsAppMediaValidator(mediaSettings);
    private final WhatsAppMediaSenderService service = new WhatsAppMediaSenderService(
            conversationService,
            contactRepository,
            messageRepository,
            settings,
            mediaClient,
            mediaStorage,
            mediaValidator
    );

    @DisplayName("Envia midia quando ultima mensagem recebida esta dentro da janela de 24 horas")
    @Test
    void sendsMediaWhenLatestInboundMessageIsWithin24HourWindow() {
        when(settings.templateSendingConfigured()).thenReturn(true);
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation());
        when(messageRepository.findLatestByConversationIdAndDirection(CONVERSATION_ID, ConversationMessageDirection.INBOUND))
                .thenReturn(Optional.of(message(ConversationMessageDirection.INBOUND, ConversationMessageStatus.RECEIVED, Instant.now().minusSeconds(3600))));
        when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact()));
        when(mediaStorage.store(any(StoreMediaCommand.class)))
                .thenReturn(new StoredMedia("local", "media/outbound/photo.jpg", "photo.jpg", "image/jpeg", 3, "sha256"));
        when(mediaClient.uploadMedia("photo.jpg", "image/jpeg", new byte[]{1, 2, 3}))
                .thenReturn(new WhatsAppMediaUploadResult(true, 200, "media-meta-001", "{\"id\":\"media-meta-001\"}"));
        when(mediaClient.sendMedia("5511999990000", WhatsAppOutboundMediaType.IMAGE, "media-meta-001", "Legenda", "photo.jpg"))
                .thenReturn(new WhatsAppMediaSendResult(true, 200, "wamid.media-001", "{\"messages\":[{\"id\":\"wamid.media-001\"}]}"));
        when(messageRepository.save(any(ConversationMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WhatsAppMediaMessageSendResult result = service.sendMedia(
                CONVERSATION_ID,
                "photo.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3},
                " Legenda ",
                authenticatedUser()
        );

        assertThat(result.type()).isEqualTo(ConversationMessageType.IMAGE);
        assertThat(result.status()).isEqualTo(ConversationMessageStatus.SENT);
        assertThat(result.externalMessageId()).isEqualTo("wamid.media-001");
        assertThat(result.mediaStorageProvider()).isEqualTo("local");
        assertThat(result.mediaStorageKey()).isEqualTo("media/outbound/photo.jpg");
        verify(mediaStorage).store(argThat(command ->
                COMPANY_ID.equals(command.companyId())
                        && STORE_ID.equals(command.storeId())
                        && "whatsapp-outbound".equals(command.source())
                        && "photo.jpg".equals(command.fileName())
        ));
        verify(messageRepository).save(argThat(message ->
                message.getType() == ConversationMessageType.IMAGE
                        && "media-meta-001".equals(message.getMediaId())
                        && "Legenda".equals(message.getContent())
                        && "sha256".equals(message.getMediaSha256())
        ));
    }

    @DisplayName("Bloqueia midia quando ultima mensagem recebida esta fora da janela de 24 horas")
    @Test
    void blocksMediaWhenLatestInboundMessageIsOutside24HourWindow() {
        when(settings.templateSendingConfigured()).thenReturn(true);
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation());
        when(messageRepository.findLatestByConversationIdAndDirection(CONVERSATION_ID, ConversationMessageDirection.INBOUND))
                .thenReturn(Optional.of(message(ConversationMessageDirection.INBOUND, ConversationMessageStatus.RECEIVED, Instant.now().minusSeconds(25 * 60 * 60))));

        assertThatThrownBy(() -> service.sendMedia(CONVERSATION_ID, "audio.ogg", "audio/ogg", new byte[]{1}, null, authenticatedUser()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Use a WhatsApp template");

        verify(mediaStorage, never()).store(any());
        verify(mediaClient, never()).uploadMedia(any(), any(), any());
        verify(messageRepository, never()).save(any());
    }

    @DisplayName("Bloqueia midia vazia antes de carregar conversa")
    @Test
    void blocksEmptyMediaBeforeLoadingConversation() {
        when(settings.templateSendingConfigured()).thenReturn(true);

        assertThatThrownBy(() -> service.sendMedia(CONVERSATION_ID, "photo.jpg", "image/jpeg", new byte[0], null, authenticatedUser()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file is empty");

        verify(conversationService, never()).getConversation(any(), any());
        verify(mediaStorage, never()).store(any());
    }

    @DisplayName("Bloqueia MIME type nao suportado antes de armazenar midia")
    @Test
    void blocksUnsupportedMimeTypeBeforeStoringMedia() {
        when(settings.templateSendingConfigured()).thenReturn(true);

        assertThatThrownBy(() -> service.sendMedia(CONVERSATION_ID, "page.html", "text/html", new byte[]{1}, null, authenticatedUser()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media MIME type is not supported");

        verify(conversationService, never()).getConversation(any(), any());
        verify(mediaStorage, never()).store(any());
    }

    @DisplayName("Bloqueia imagem acima do limite configurado antes de armazenar midia")
    @Test
    void blocksImageAboveConfiguredLimitBeforeStoringMedia() {
        when(settings.templateSendingConfigured()).thenReturn(true);
        when(mediaSettings.maxImageSizeBytes()).thenReturn(2L);

        assertThatThrownBy(() -> service.sendMedia(CONVERSATION_ID, "photo.jpg", "image/jpeg", new byte[]{1, 2, 3}, null, authenticatedUser()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file exceeds the configured size limit");

        verify(conversationService, never()).getConversation(any(), any());
        verify(mediaStorage, never()).store(any());
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
        return new WhatsAppContact(CONTACT_ID, COMPANY_ID, STORE_ID, null, "+5511999990000", "Cliente", Instant.now(), Instant.now());
    }

    private ConversationMessage message(ConversationMessageDirection direction, ConversationMessageStatus status, Instant createdAt) {
        return new ConversationMessage(
                UUID.randomUUID(),
                CONVERSATION_ID,
                direction,
                ConversationMessageType.TEXT,
                status,
                null,
                "Oi",
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
