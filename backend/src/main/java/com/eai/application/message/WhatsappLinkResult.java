package com.eai.application.message;

import java.util.UUID;

public record WhatsappLinkResult(UUID leadId, UUID templateId, UUID communicationId, String message, String url) {
}
