package com.eai.application.whatsapp;

import com.eai.application.conversation.ConversationService;
import com.eai.domain.conversation.ConversationMessageStatus;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WhatsAppWebhookServiceTest {

    private final WhatsAppChannelSettings settings = mock(WhatsAppChannelSettings.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppWebhookService service = new WhatsAppWebhookService(settings, conversationService, new ObjectMapper());

    @Test
    void updatesMessageStatusFromProviderWebhookEvent() {
        service.receiveEvent("""
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"statuses":[{"id":"wamid.text-001","status":"delivered"}]}}]}]}
                """);

        verify(conversationService).updateMessageStatusByExternalId("wamid.text-001", ConversationMessageStatus.DELIVERED);
    }
}
