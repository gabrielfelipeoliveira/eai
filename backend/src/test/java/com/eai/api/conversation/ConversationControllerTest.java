package com.eai.api.conversation;

import com.eai.application.conversation.ConversationFilters;
import com.eai.application.conversation.ConversationMediaDownload;
import com.eai.application.conversation.ConversationMediaService;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.ConversationSummary;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.whatsapp.WhatsAppMediaMessageSendResult;
import com.eai.application.whatsapp.WhatsAppMediaSenderService;
import com.eai.application.whatsapp.WhatsAppMediaValidator;
import com.eai.application.whatsapp.WhatsAppTextSendResult;
import com.eai.application.whatsapp.WhatsAppTextSenderService;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();
    private static final AuthenticatedUser USER = new AuthenticatedUser(USER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));

    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppTextSenderService textSenderService = mock(WhatsAppTextSenderService.class);
    private final WhatsAppMediaSenderService mediaSenderService = mock(WhatsAppMediaSenderService.class);
    private final ConversationMediaService mediaService = mock(ConversationMediaService.class);
    private final WhatsAppMediaValidator mediaValidator = mock(WhatsAppMediaValidator.class);
    private final ConversationController controller = new ConversationController(
            conversationService,
            textSenderService,
            mediaSenderService,
            mediaService,
            mediaValidator
    );

    @DisplayName("Lista conversas repassando filtros para o servico")
    @Test
    void listConversationsDelegatesFilters() {
        Instant start = Instant.parse("2026-07-01T00:00:00Z");
        Instant end = Instant.parse("2026-07-02T00:00:00Z");
        when(conversationService.listConversationSummaries(eq(USER), any(ConversationFilters.class))).thenReturn(List.of(summary()));

        List<ConversationSummaryResponse> responses = controller.listConversations(USER_ID, ConversationMessageStatus.RECEIVED, start, end, USER);

        ArgumentCaptor<ConversationFilters> captor = ArgumentCaptor.forClass(ConversationFilters.class);
        verify(conversationService).listConversationSummaries(eq(USER), captor.capture());
        assertThat(captor.getValue().sellerId()).isEqualTo(USER_ID);
        assertThat(responses.getFirst().lastMessageContent()).isEqualTo("Oi");
    }

    @DisplayName("Busca conversa e mensagens convertendo dominio para resposta")
    @Test
    void getConversationAndMessagesMapDomain() {
        when(conversationService.getConversation(CONVERSATION_ID, USER)).thenReturn(conversation());
        when(conversationService.listMessages(CONVERSATION_ID, USER)).thenReturn(List.of(message()));

        assertThat(controller.getConversation(CONVERSATION_ID, USER).id()).isEqualTo(CONVERSATION_ID);
        assertThat(controller.listMessages(CONVERSATION_ID, USER).getFirst().content()).isEqualTo("Oi");
    }

    @DisplayName("Envia texto pelo WhatsApp e converte resultado")
    @Test
    void sendTextMessageMapsResult() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        when(textSenderService.sendText(CONVERSATION_ID, "Ola", USER)).thenReturn(new WhatsAppTextSendResult(
                CONVERSATION_ID, MESSAGE_ID, ConversationMessageStatus.SENT, "wamid.1", "Ola", "{}", now, now
        ));

        ConversationMessageResponse response = controller.sendTextMessage(CONVERSATION_ID, new ConversationTextMessageRequest("Ola"), USER);

        assertThat(response.id()).isEqualTo(MESSAGE_ID);
        assertThat(response.direction()).isEqualTo(ConversationMessageDirection.OUTBOUND);
    }

    @DisplayName("Envia midia validando upload antes de chamar servico")
    @Test
    void sendMediaMessageValidatesUploadAndMapsResult() throws Exception {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[]{1, 2, 3});
        when(mediaSenderService.sendMedia(eq(CONVERSATION_ID), eq("foto.png"), eq("image/png"), any(byte[].class), eq("Legenda"), eq(USER)))
                .thenReturn(new WhatsAppMediaMessageSendResult(
                        CONVERSATION_ID, MESSAGE_ID, ConversationMessageType.IMAGE, ConversationMessageStatus.SENT,
                        "wamid.2", "media-1", "image/png", "local", "key", "foto.png", 3L, "sha", "Legenda", "{}", now, now
                ));

        ConversationMessageResponse response = controller.sendMediaMessage(CONVERSATION_ID, file, "Legenda", USER);

        verify(mediaValidator).validateUpload("image/png", 3L);
        assertThat(response.mediaFileName()).isEqualTo("foto.png");
        assertThat(response.type()).isEqualTo(ConversationMessageType.IMAGE);
    }

    @DisplayName("Download de midia retorna headers e conteudo do arquivo")
    @Test
    void downloadMediaReturnsContentHeaders() {
        when(mediaService.download(CONVERSATION_ID, MESSAGE_ID, USER))
                .thenReturn(new ConversationMediaDownload("contrato.pdf", "application/pdf", 3, new byte[]{1, 2, 3}));

        ResponseEntity<byte[]> response = controller.downloadMedia(CONVERSATION_ID, MESSAGE_ID, USER);

        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(3);
        assertThat(response.getBody()).containsExactly(1, 2, 3);
    }

    private ConversationSummary summary() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new ConversationSummary(
                CONVERSATION_ID, COMPANY_ID, STORE_ID, UUID.randomUUID(), UUID.randomUUID(), USER_ID,
                "Cliente", "+5511999990000", "Cliente", MESSAGE_ID,
                ConversationMessageDirection.INBOUND, ConversationMessageType.TEXT, ConversationMessageStatus.RECEIVED,
                "Oi", now, 1, now, now
        );
    }

    private Conversation conversation() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Conversation(CONVERSATION_ID, COMPANY_ID, STORE_ID, UUID.randomUUID(), UUID.randomUUID(), USER_ID, now, now);
    }

    private ConversationMessage message() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new ConversationMessage(
                MESSAGE_ID,
                CONVERSATION_ID,
                ConversationMessageDirection.INBOUND,
                ConversationMessageType.TEXT,
                ConversationMessageStatus.RECEIVED,
                "wamid.1",
                "Oi",
                null,
                null,
                "{}",
                now,
                now
        );
    }
}
