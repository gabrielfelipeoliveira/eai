package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.IncomingWhatsAppMessage;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.media.StoreMediaCommand;
import com.eai.application.media.StoredMedia;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WhatsAppWebhookServiceTest {

    private static final String APP_SECRET = "test-secret";

    private final WhatsAppChannelSettings settings = mock(WhatsAppChannelSettings.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppMediaClient mediaClient = mock(WhatsAppMediaClient.class);
    private final MediaStoragePort mediaStorage = mock(MediaStoragePort.class);
    private final WhatsAppMediaSettings mediaSettings = mock(WhatsAppMediaSettings.class);
    private final WhatsAppMediaValidator mediaValidator = new WhatsAppMediaValidator(mediaSettings);
    private final WhatsAppWebhookService service = new WhatsAppWebhookService(settings, conversationService, mediaClient, mediaStorage, new ObjectMapper(), mediaValidator);

    @DisplayName("Atualiza status da mensagem a partir do webhook do provedor")
    @Test
    void updatesMessageStatusFromProviderWebhookEvent() throws Exception {
        String payload = """
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"statuses":[{"id":"wamid.text-001","status":"delivered","timestamp":"1783526400"}]}}]}]}
                """;

        receiveSignedEvent(payload);

        verify(conversationService).recordMessageStatusEvent(
                "wamid.text-001",
                ConversationMessageStatus.DELIVERED,
                null,
                "{\"id\":\"wamid.text-001\",\"status\":\"delivered\",\"timestamp\":\"1783526400\"}",
                Instant.parse("2026-07-08T16:00:00Z")
        );
    }

    @DisplayName("Interpreta eventos de status enviado, lido e falha do provedor")
    @Test
    void parsesSentReadAndFailedStatusEventsFromProviderWebhookEvent() throws Exception {
        String payload = """
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"statuses":[{"id":"wamid.text-002","status":"sent","timestamp":"1783526401"},{"id":"wamid.text-003","status":"read","timestamp":"1783526402"},{"id":"wamid.text-004","status":"failed","timestamp":"1783526403","errors":[{"code":131026,"title":"Message undeliverable","message":"Message undeliverable","error_data":{"details":"Recipient phone number is invalid"}}]}]}}]}]}
                """;

        receiveSignedEvent(payload);

        verify(conversationService).recordMessageStatusEvent(
                "wamid.text-002",
                ConversationMessageStatus.SENT,
                null,
                "{\"id\":\"wamid.text-002\",\"status\":\"sent\",\"timestamp\":\"1783526401\"}",
                Instant.parse("2026-07-08T16:00:01Z")
        );
        verify(conversationService).recordMessageStatusEvent(
                "wamid.text-003",
                ConversationMessageStatus.READ,
                null,
                "{\"id\":\"wamid.text-003\",\"status\":\"read\",\"timestamp\":\"1783526402\"}",
                Instant.parse("2026-07-08T16:00:02Z")
        );
        verify(conversationService).recordMessageStatusEvent(
                "wamid.text-004",
                ConversationMessageStatus.FAILED,
                "Recipient phone number is invalid",
                "{\"id\":\"wamid.text-004\",\"status\":\"failed\",\"timestamp\":\"1783526403\",\"errors\":[{\"code\":131026,\"title\":\"Message undeliverable\",\"message\":\"Message undeliverable\",\"error_data\":{\"details\":\"Recipient phone number is invalid\"}}]}",
                Instant.parse("2026-07-08T16:00:03Z")
        );
    }

    @DisplayName("Armazena midia recebida antes de registrar a mensagem")
    @Test
    void storesInboundMediaBeforeRecordingMessage() throws Exception {
        UUID companyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        when(settings.inboundPersistenceConfigured()).thenReturn(true);
        when(settings.companyId()).thenReturn(companyId.toString());
        when(settings.storeId()).thenReturn(storeId.toString());
        when(conversationService.incomingMessageAlreadyRecorded("wamid.image-001")).thenReturn(false);
        when(mediaClient.fetchMediaMetadata("media-001"))
                .thenReturn(new WhatsAppMediaMetadata("media-001", "https://graph.example/media", "image/jpeg", 3L, "abc123", "{\"id\":\"media-001\"}"));
        when(mediaClient.downloadMedia(any(WhatsAppMediaMetadata.class)))
                .thenReturn(new WhatsAppMediaDownload(new byte[]{1, 2, 3}, "{\"id\":\"media-001\"}"));
        when(mediaStorage.store(any(StoreMediaCommand.class)))
                .thenReturn(new StoredMedia("local", "company/store/media.jpg", "media-001.jpeg", "image/jpeg", 3, "abc123"));
        String payload = """
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"contacts":[{"profile":{"name":"Cliente"}}],"messages":[{"from":"5511999990000","id":"wamid.image-001","type":"image","image":{"id":"media-001","mime_type":"image/jpeg","sha256":"abc123","caption":"Foto"}}]}}]}]}
                """;

        receiveSignedEvent(payload);

        verify(mediaClient).fetchMediaMetadata("media-001");
        verify(mediaStorage).store(argThat(command ->
                companyId.equals(command.companyId())
                        && storeId.equals(command.storeId())
                        && "whatsapp-inbound".equals(command.source())
                        && "media-001".equals(command.externalMediaId())
        ));
        verify(conversationService).recordIncomingMessage(org.mockito.ArgumentMatchers.eq(companyId), org.mockito.ArgumentMatchers.eq(storeId), argThat((IncomingWhatsAppMessage message) ->
                message.type() == ConversationMessageType.IMAGE
                        && "local".equals(message.mediaStorageProvider())
                        && "company/store/media.jpg".equals(message.mediaStorageKey())
                        && "abc123".equals(message.mediaSha256())
        ));
    }

    @DisplayName("Nao baixa midia de mensagem recebida duplicada")
    @Test
    void doesNotDownloadMediaForDuplicateInboundMessage() throws Exception {
        when(settings.inboundPersistenceConfigured()).thenReturn(true);
        when(settings.companyId()).thenReturn("00000000-0000-0000-0000-000000000101");
        when(settings.storeId()).thenReturn("00000000-0000-0000-0000-000000000201");
        when(conversationService.incomingMessageAlreadyRecorded("wamid.image-001")).thenReturn(true);
        String payload = """
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"messages":[{"from":"5511999990000","id":"wamid.image-001","type":"image","image":{"id":"media-001","mime_type":"image/jpeg"}}]}}]}]}
                """;

        receiveSignedEvent(payload);

        verify(mediaClient, never()).fetchMediaMetadata(any());
        verify(mediaStorage, never()).store(any());
    }

    @DisplayName("Nao baixa midia recebida quando metadados excedem limite configurado")
    @Test
    void doesNotDownloadInboundMediaWhenMetadataExceedsConfiguredLimit() throws Exception {
        when(settings.inboundPersistenceConfigured()).thenReturn(true);
        when(settings.companyId()).thenReturn("00000000-0000-0000-0000-000000000101");
        when(settings.storeId()).thenReturn("00000000-0000-0000-0000-000000000201");
        when(conversationService.incomingMessageAlreadyRecorded("wamid.image-001")).thenReturn(false);
        when(mediaSettings.maxImageSizeBytes()).thenReturn(2L);
        when(mediaClient.fetchMediaMetadata("media-001"))
                .thenReturn(new WhatsAppMediaMetadata("media-001", "https://graph.example/media", "image/jpeg", 3L, "abc123", "{\"id\":\"media-001\"}"));
        String payload = """
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"messages":[{"from":"5511999990000","id":"wamid.image-001","type":"image","image":{"id":"media-001","mime_type":"image/jpeg"}}]}}]}]}
                """;

        assertThatThrownBy(() -> receiveSignedEvent(payload))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file exceeds the configured size limit");

        verify(mediaClient).fetchMediaMetadata("media-001");
        verify(mediaClient, never()).downloadMedia(any());
        verify(mediaStorage, never()).store(any());
    }

    private void receiveSignedEvent(String payload) throws Exception {
        when(settings.appSecret()).thenReturn(APP_SECRET);
        service.receiveEvent(signatureFor(payload), payload);
    }

    private String signatureFor(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
