package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.IncomingWhatsAppMessage;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.media.StoreMediaCommand;
import com.eai.application.media.StoredMedia;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhatsAppWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookService.class);

    private final WhatsAppChannelSettings settings;
    private final ConversationService conversationService;
    private final WhatsAppMediaClient mediaClient;
    private final MediaStoragePort mediaStorage;
    private final ObjectMapper objectMapper;

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
        List<WhatsAppMessageStatusEvent> statuses = parseStatusEvents(payload);
        statuses.forEach(status -> conversationService.recordMessageStatusEvent(
                status.externalMessageId(),
                status.status(),
                status.failureReason(),
                status.rawPayload(),
                status.occurredAt()
        ));
        if (messages.isEmpty()) {
            logger.info("WhatsApp webhook event received with {} status update(s) and without messages", statuses.size());
            return;
        }
        if (!settings.inboundPersistenceConfigured()) {
            throw new ApplicationException("WHATSAPP_INBOUND_TENANT_NOT_CONFIGURED", "WhatsApp inbound company and store are not configured");
        }
        UUID companyId = UUID.fromString(settings.companyId());
        UUID storeId = UUID.fromString(settings.storeId());
        messages.forEach(message -> conversationService.recordIncomingMessage(companyId, storeId, storeMediaIfNeeded(companyId, storeId, message)));
        logger.info("WhatsApp webhook persisted {} incoming message(s) and processed {} status update(s)", messages.size(), statuses.size());
    }

    private IncomingWhatsAppMessage storeMediaIfNeeded(UUID companyId, UUID storeId, IncomingWhatsAppMessage message) {
        if (!hasMedia(message) || conversationService.incomingMessageAlreadyRecorded(message.externalMessageId())) {
            return message;
        }
        WhatsAppMediaMetadata metadata = mediaClient.fetchMediaMetadata(message.mediaId());
        WhatsAppMediaDownload download = mediaClient.downloadMedia(metadata);
        String mimeType = firstNonBlank(metadata.mimeType(), message.mediaMimeType(), "application/octet-stream");
        StoredMedia storedMedia = mediaStorage.store(new StoreMediaCommand(
                companyId,
                storeId,
                "whatsapp-inbound",
                message.mediaId(),
                extractMediaFileName(message),
                mimeType,
                download.content(),
                metadata.sha256()
        ));
        return new IncomingWhatsAppMessage(
                message.phone(),
                message.contactName(),
                message.type(),
                message.externalMessageId(),
                message.content(),
                message.mediaId(),
                mimeType,
                storedMedia.provider(),
                storedMedia.key(),
                storedMedia.fileName(),
                storedMedia.sizeBytes(),
                storedMedia.sha256(),
                message.rawPayload()
        );
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
                                null,
                                null,
                                extractMediaFileName(message, typeCode),
                                null,
                                extractMediaSha256(message, typeCode),
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

    private List<WhatsAppMessageStatusEvent> parseStatusEvents(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            List<WhatsAppMessageStatusEvent> result = new ArrayList<>();
            for (JsonNode entry : iterable(root.path("entry"))) {
                for (JsonNode change : iterable(entry.path("changes"))) {
                    JsonNode value = change.path("value");
                    for (JsonNode status : iterable(value.path("statuses"))) {
                        String messageId = text(status.path("id"));
                        ConversationMessageStatus messageStatus = toMessageStatus(text(status.path("status")));
                        if (messageId != null && messageStatus != null) {
                            result.add(new WhatsAppMessageStatusEvent(
                                    messageId,
                                    messageStatus,
                                    extractFailureReason(status),
                                    extractTimestamp(status),
                                    status.toString()
                            ));
                        }
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

    private ConversationMessageStatus toMessageStatus(String status) {
        return switch (status == null ? "" : status) {
            case "sent" -> ConversationMessageStatus.SENT;
            case "delivered" -> ConversationMessageStatus.DELIVERED;
            case "read" -> ConversationMessageStatus.READ;
            case "failed" -> ConversationMessageStatus.FAILED;
            default -> null;
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

    private String extractMediaFileName(JsonNode message, String type) {
        return switch (type == null ? "" : type) {
            case "document" -> text(message.path("document").path("filename"));
            default -> null;
        };
    }

    private String extractMediaSha256(JsonNode message, String type) {
        return switch (type == null ? "" : type) {
            case "image" -> text(message.path("image").path("sha256"));
            case "audio", "voice" -> text(message.path("audio").path("sha256"));
            case "document" -> text(message.path("document").path("sha256"));
            default -> null;
        };
    }

    private boolean hasMedia(IncomingWhatsAppMessage message) {
        return message.mediaId() != null && switch (message.type()) {
            case IMAGE, AUDIO, DOCUMENT -> true;
            default -> false;
        };
    }

    private String extractMediaFileName(IncomingWhatsAppMessage message) {
        if (message.mediaFileName() != null) {
            return message.mediaFileName();
        }
        String extension = switch (message.type()) {
            case IMAGE -> extensionFor(message.mediaMimeType(), "jpg");
            case AUDIO -> extensionFor(message.mediaMimeType(), "ogg");
            case DOCUMENT -> "bin";
            default -> "bin";
        };
        return message.mediaId() + "." + extension;
    }

    private String extensionFor(String mimeType, String fallback) {
        if (mimeType == null || !mimeType.contains("/")) {
            return fallback;
        }
        String extension = mimeType.substring(mimeType.indexOf('/') + 1).replaceAll("[^A-Za-z0-9]", "");
        return extension.isBlank() ? fallback : extension;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Instant extractTimestamp(JsonNode status) {
        String timestamp = text(status.path("timestamp"));
        if (timestamp == null) {
            return null;
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(timestamp));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String extractFailureReason(JsonNode status) {
        JsonNode errors = status.path("errors");
        if (!errors.isArray() || errors.isEmpty()) {
            return null;
        }
        JsonNode error = errors.get(0);
        String details = text(error.path("error_data").path("details"));
        if (details != null) {
            return details;
        }
        String message = text(error.path("message"));
        if (message != null) {
            return message;
        }
        String title = text(error.path("title"));
        if (title != null) {
            return title;
        }
        String code = text(error.path("code"));
        return code == null ? null : "WhatsApp error " + code;
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
