package com.eai.application.conversation;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.NotFoundException;
import com.eai.application.media.MediaObject;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.whatsapp.WhatsAppMediaValidator;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationMediaServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONVERSATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID OTHER_CONVERSATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000402");
    private static final UUID MESSAGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000601");

    private final ConversationService conversationService = mock(ConversationService.class);
    private final ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
    private final MediaStoragePort mediaStorage = mock(MediaStoragePort.class);
    private final WhatsAppMediaValidator mediaValidator = mock(WhatsAppMediaValidator.class);
    private final ConversationMediaService service = new ConversationMediaService(conversationService, messageRepository, mediaStorage, mediaValidator);

    @DisplayName("Baixa midia armazenada apos validar acesso a conversa")
    @Test
    void downloadsStoredMediaAfterConversationAccessIsValidated() {
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation(CONVERSATION_ID));
        when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(message(CONVERSATION_ID)));
        when(mediaStorage.read("local", "media/inbound/photo.jpg"))
                .thenReturn(new MediaObject("local", "media/inbound/photo.jpg", "photo.jpg", "image/jpeg", 3, "sha256", new byte[]{1, 2, 3}));

        ConversationMediaDownload download = service.download(CONVERSATION_ID, MESSAGE_ID, authenticatedUser());

        assertThat(download.fileName()).isEqualTo("photo.jpg");
        assertThat(download.mimeType()).isEqualTo("image/jpeg");
        assertThat(download.sizeBytes()).isEqualTo(3);
        assertThat(download.content()).containsExactly(1, 2, 3);
        verify(conversationService).getConversation(CONVERSATION_ID, authenticatedUser());
        verify(mediaValidator).validateDownload("image/jpeg", 3L);
        verify(mediaStorage).read("local", "media/inbound/photo.jpg");
    }

    @DisplayName("Rejeita mensagem de outra conversa antes de ler a midia")
    @Test
    void rejectsMessageFromAnotherConversation() {
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation(CONVERSATION_ID));
        when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(message(OTHER_CONVERSATION_ID)));

        assertThatThrownBy(() -> service.download(CONVERSATION_ID, MESSAGE_ID, authenticatedUser()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Conversation message not found");

        verify(mediaStorage, never()).read(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @DisplayName("Bloqueia download quando metadados da mensagem violam limite de midia")
    @Test
    void blocksDownloadWhenMessageMetadataViolatesMediaLimit() {
        when(conversationService.getConversation(CONVERSATION_ID, authenticatedUser())).thenReturn(conversation(CONVERSATION_ID));
        when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(message(CONVERSATION_ID)));
        org.mockito.Mockito.doThrow(new ApplicationException("WHATSAPP_MEDIA_FILE_TOO_LARGE", "Media file exceeds the configured size limit"))
                .when(mediaValidator).validateDownload("image/jpeg", Long.valueOf(3L));

        assertThatThrownBy(() -> service.download(CONVERSATION_ID, MESSAGE_ID, authenticatedUser()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file exceeds the configured size limit");

        verify(mediaStorage, never()).read(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private Conversation conversation(UUID conversationId) {
        return new Conversation(
                conversationId,
                COMPANY_ID,
                STORE_ID,
                UUID.fromString("00000000-0000-0000-0000-000000000501"),
                null,
                USER_ID,
                Instant.parse("2026-07-08T10:00:00Z"),
                Instant.parse("2026-07-08T10:00:00Z")
        );
    }

    private ConversationMessage message(UUID conversationId) {
        return new ConversationMessage(
                MESSAGE_ID,
                conversationId,
                ConversationMessageDirection.INBOUND,
                ConversationMessageType.IMAGE,
                ConversationMessageStatus.RECEIVED,
                "wamid.image-001",
                "Foto",
                "media-meta-001",
                "image/jpeg",
                "local",
                "media/inbound/photo.jpg",
                "photo.jpg",
                3L,
                "sha256",
                "{\"image\":true}",
                Instant.parse("2026-07-08T10:00:00Z"),
                Instant.parse("2026-07-08T10:00:00Z")
        );
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(USER_ID, "admin@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.ADMIN));
    }
}
