package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.IncomingWhatsAppMessage;
import com.eai.domain.conversation.ConversationMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WhatsAppWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookService.class);

    private final WhatsAppChannelSettings settings;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookService(WhatsAppChannelSettings settings, ConversationService conversationService, ObjectMapper objectMapper) {
        this.settings = settings;
        this.conversationService = conversationService;
        this.objectMapper = objectMapper;
    }

    public String verifyWebhook(String mode, String verifyToken, String challenge) {
        if (!settings.webhookConfigured()) {
            throw new ApplicationException("WHATSAPP_WEBHOOK_NOT_CONFIGURED", "WhatsApp webhook verification token is not configured");
        }
        if (!"subscribe".equals(mode) || !settings.verifyToken().equals(verifyToken)) {
            throw new ForbiddenException("Invalid WhatsApp webhook verification request");
        }
        logger.info("WhatsApp webhook verified successfully");
        return challenge;
    }

    public void receiveEvent(String payload) {
        List<IncomingWhatsAppMessage> messages = parseIncomingMessages(payload);
        if (messages.isEmpty()) {
            logger.info("WhatsApp webhook event received without messages");
            return;
        }
        if (!settings.inboundPersistenceConfigured()) {
            throw new ApplicationException("WHATSAPP_INBOUND_TENANT_NOT_CONFIGURED", "WhatsApp inbound company and store are not configured");
        }
        UUID companyId = UUID.fromString(settings.companyId());
        UUID storeId = UUID.fromString(settings.storeId());
        messages.forEach(message -> conversationService.recordIncomingMessage(companyId, storeId, message));
        logger.info("WhatsApp webhook persisted {} incoming message(s)", messages.size());
    }

    private List<IncomingWhatsAppMessage> parseIncomingMessages(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            List<IncomingWhatsAppMessage> result = new ArrayList<>();
            for (JsonNode entry : iterable(root.path("entry"))) {
                for (JsonNode change : iterable(entry.path("changes"))) {
                    JsonNode value = change.path("value");
                    String contactName = firstContactName(value.path("contacts"));
                    for (JsonNode message : iterable(value.path("messages"))) {
                        String from = text(message.path("from"));
                        if (from == null) {
                            continue;
                        }
                        String typeCode = text(message.path("type"));
                        ConversationMessageType type = toMessageType(typeCode);
                        result.add(new IncomingWhatsAppMessage(
                                from,
                                contactName,
                                type,
                                text(message.path("id")),
                                extractContent(message, typeCode),
                                extractMediaId(message, typeCode),
                                extractMediaMimeType(message, typeCode),
                                message.toString()
                        ));
                    }
                }
            }
            return result;
        } catch (Exception exception) {
            throw new ApplicationException("INVALID_WHATSAPP_WEBHOOK_PAYLOAD", "Invalid WhatsApp webhook payload");
        }
    }

    private Iterable<JsonNode> iterable(JsonNode node) {
        return node.isArray() ? node : List.of();
    }

    private String firstContactName(JsonNode contacts) {
        if (!contacts.isArray() || contacts.isEmpty()) {
            return null;
        }
        return text(contacts.get(0).path("profile").path("name"));
    }

    private ConversationMessageType toMessageType(String type) {
        return switch (type == null ? "" : type) {
            case "template" -> ConversationMessageType.TEMPLATE;
            case "image" -> ConversationMessageType.IMAGE;
            case "audio", "voice" -> ConversationMessageType.AUDIO;
            case "document" -> ConversationMessageType.DOCUMENT;
            default -> ConversationMessageType.TEXT;
        };
    }

    private String extractContent(JsonNode message, String type) {
        return switch (type == null ? "" : type) {
            case "text" -> text(message.path("text").path("body"));
            case "template" -> text(message.path("template").path("name"));
            case "image" -> text(message.path("image").path("caption"));
            case "document" -> text(message.path("document").path("caption"));
            default -> null;
        };
    }

    private String extractMediaId(JsonNode message, String type) {
        return switch (type == null ? "" : type) {
            case "image" -> text(message.path("image").path("id"));
            case "audio", "voice" -> text(message.path("audio").path("id"));
            case "document" -> text(message.path("document").path("id"));
            default -> null;
        };
    }

    private String extractMediaMimeType(JsonNode message, String type) {
        return switch (type == null ? "" : type) {
            case "image" -> text(message.path("image").path("mime_type"));
            case "audio", "voice" -> text(message.path("audio").path("mime_type"));
            case "document" -> text(message.path("document").path("mime_type"));
            default -> null;
        };
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
