package com.eai.api.message;

import com.eai.application.message.WhatsappLinkResult;

import java.util.UUID;

public record WhatsappLinkResponse(UUID leadId, UUID templateId, UUID communicationId, String message, String url) {

    public static WhatsappLinkResponse fromResult(WhatsappLinkResult result) {
        return new WhatsappLinkResponse(result.leadId(), result.templateId(), result.communicationId(), result.message(), result.url());
    }
}
