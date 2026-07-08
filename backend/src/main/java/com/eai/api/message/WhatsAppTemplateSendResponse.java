package com.eai.api.message;

import com.eai.application.whatsapp.WhatsAppTemplateSendResult;
import com.eai.domain.conversation.ConversationMessageStatus;

import java.util.UUID;

public record WhatsAppTemplateSendResponse(
        UUID leadId,
        UUID templateId,
        UUID communicationId,
        UUID conversationMessageId,
        ConversationMessageStatus status,
        String externalMessageId,
        String message,
        String providerResponse
) {

    public static WhatsAppTemplateSendResponse fromResult(WhatsAppTemplateSendResult result) {
        return new WhatsAppTemplateSendResponse(
                result.leadId(),
                result.templateId(),
                result.communicationId(),
                result.conversationMessageId(),
                result.status(),
                result.externalMessageId(),
                result.message(),
                result.providerResponse()
        );
    }
}
