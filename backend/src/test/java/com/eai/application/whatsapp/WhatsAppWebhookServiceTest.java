package com.eai.application.whatsapp;

import com.eai.application.conversation.ConversationService;
import com.eai.domain.conversation.ConversationMessageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WhatsAppWebhookServiceTest {

    private final WhatsAppChannelSettings settings = mock(WhatsAppChannelSettings.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppWebhookService service = new WhatsAppWebhookService(settings, conversationService, new ObjectMapper());

    @DisplayName("Atualiza status da mensagem a partir do webhook do provedor")
    @Test
    void updatesMessageStatusFromProviderWebhookEvent() {
        service.receiveEvent("""
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"statuses":[{"id":"wamid.text-001","status":"delivered","timestamp":"1783526400"}]}}]}]}
                """);

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
    void parsesSentReadAndFailedStatusEventsFromProviderWebhookEvent() {
        service.receiveEvent("""
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"statuses":[
                  {"id":"wamid.text-002","status":"sent","timestamp":"1783526401"},
                  {"id":"wamid.text-003","status":"read","timestamp":"1783526402"},
                  {"id":"wamid.text-004","status":"failed","timestamp":"1783526403","errors":[{"code":131026,"title":"Message undeliverable","message":"Message undeliverable","error_data":{"details":"Recipient phone number is invalid"}}]}
                ]}}]}]}
                """);

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
}
