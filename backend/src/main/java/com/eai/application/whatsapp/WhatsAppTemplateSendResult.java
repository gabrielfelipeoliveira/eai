package com.eai.application.whatsapp;

import com.eai.domain.conversation.ConversationMessageStatus;

import java.util.UUID;

public record WhatsAppTemplateSendResult(
        UUID leadId,
        UUID templateId,
        UUID communicationId,
        UUID conversationMessageId,
        ConversationMessageStatus status,
        String externalMessageId,
        String message,
        String providerResponse
) {
}
