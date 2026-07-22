package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.NotFoundException;
import com.eai.application.conversation.ConversationMessageRepository;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.WhatsAppContactRepository;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.media.StoreMediaCommand;
import com.eai.application.media.StoredMedia;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.conversation.WhatsAppContact;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class WhatsAppMediaSenderService {

    private static final Duration FREE_TEXT_WINDOW = Duration.ofHours(24);

    private final ConversationService conversationService;
    private final WhatsAppContactRepository contactRepository;
    private final ConversationMessageRepository messageRepository;
    private final WhatsAppChannelSettings settings;
    private final WhatsAppMediaClient mediaClient;
    private final MediaStoragePort mediaStorage;

    public WhatsAppMediaSenderService(
            ConversationService conversationService,
            WhatsAppContactRepository contactRepository,
            ConversationMessageRepository messageRepository,
            WhatsAppChannelSettings settings,
            WhatsAppMediaClient mediaClient,
            MediaStoragePort mediaStorage
    ) {
        this.conversationService = conversationService;
        this.contactRepository = contactRepository;
        this.messageRepository = messageRepository;
        this.settings = settings;
        this.mediaClient = mediaClient;
        this.mediaStorage = mediaStorage;
    }

    @Transactional
    public WhatsAppMediaMessageSendResult sendMedia(
            UUID conversationId,
            String fileName,
            String mimeType,
            byte[] content,
            String caption,
            AuthenticatedUser authenticatedUser
    ) {
        if (!settings.templateSendingConfigured()) {
            throw new ApplicationException("WHATSAPP_MEDIA_SENDING_NOT_CONFIGURED", "WhatsApp media sending is not configured");
        }
        if (content == null || content.length == 0) {
            throw new ApplicationException("WHATSAPP_MEDIA_FILE_EMPTY", "Media file is empty");
        }
        Conversation conversation = conversationService.getConversation(conversationId, authenticatedUser);
        assertWithinFreeTextWindow(conversation.getId());
        WhatsAppContact contact = contactRepository.findById(conversation.getContactId())
                .orElseThrow(() -> new NotFoundException("WhatsApp contact not found"));

        ConversationMessageType messageType = messageType(mimeType);
        String normalizedCaption = normalizeCaption(caption);
        StoredMedia storedMedia = mediaStorage.store(new StoreMediaCommand(
                conversation.getCompanyId(),
                conversation.getStoreId(),
                "whatsapp-outbound",
                UUID.randomUUID().toString(),
                fileName,
                normalizeMimeType(mimeType),
                content,
                null
        ));

        WhatsAppMediaUploadResult uploadResult = mediaClient.uploadMedia(storedMedia.fileName(), storedMedia.mimeType(), content);
        WhatsAppMediaSendResult sendResult = uploadResult.successful()
                ? mediaClient.sendMedia(
                        toWhatsappPhone(contact.getPhone()),
                        outboundType(messageType),
                        uploadResult.mediaId(),
                        normalizedCaption,
                        storedMedia.fileName()
                )
                : new WhatsAppMediaSendResult(false, uploadResult.statusCode(), null, uploadResult.rawResponse());

        ConversationMessageStatus status = sendResult.successful()
                ? ConversationMessageStatus.SENT
                : ConversationMessageStatus.FAILED;
        ConversationMessage message = ConversationMessage.outbound(
                conversation.getId(),
                messageType,
                status,
                sendResult.externalMessageId(),
                normalizedCaption,
                uploadResult.mediaId(),
                storedMedia.mimeType(),
                storedMedia.provider(),
                storedMedia.key(),
                storedMedia.fileName(),
                storedMedia.sizeBytes(),
                storedMedia.sha256(),
                sendResult.rawResponse()
        );
        ConversationMessage saved = messageRepository.save(message);
        return new WhatsAppMediaMessageSendResult(
                saved.getConversationId(),
                saved.getId(),
                saved.getType(),
                saved.getStatus(),
                saved.getExternalMessageId(),
                saved.getMediaId(),
                saved.getMediaMimeType(),
                saved.getMediaStorageProvider(),
                saved.getMediaStorageKey(),
                saved.getMediaFileName(),
                saved.getMediaSizeBytes(),
                saved.getMediaSha256(),
                saved.getContent(),
                sendResult.rawResponse(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    private void assertWithinFreeTextWindow(UUID conversationId) {
        ConversationMessage latestInbound = messageRepository.findLatestByConversationIdAndDirection(conversationId, ConversationMessageDirection.INBOUND)
                .orElseThrow(() -> new ApplicationException(
                        "WHATSAPP_FREE_TEXT_WINDOW_EXPIRED",
                        "Free text messages require a customer message in the last 24 hours. Use a WhatsApp template."
                ));
        if (latestInbound.getCreatedAt().isBefore(Instant.now().minus(FREE_TEXT_WINDOW))) {
            throw new ApplicationException(
                    "WHATSAPP_FREE_TEXT_WINDOW_EXPIRED",
                    "Free text messages require a customer message in the last 24 hours. Use a WhatsApp template."
            );
        }
    }

    private ConversationMessageType messageType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("image/")) {
            return ConversationMessageType.IMAGE;
        }
        if (mimeType != null && mimeType.startsWith("audio/")) {
            return ConversationMessageType.AUDIO;
        }
        return ConversationMessageType.DOCUMENT;
    }

    private WhatsAppOutboundMediaType outboundType(ConversationMessageType type) {
        return switch (type) {
            case IMAGE -> WhatsAppOutboundMediaType.IMAGE;
            case AUDIO -> WhatsAppOutboundMediaType.AUDIO;
            default -> WhatsAppOutboundMediaType.DOCUMENT;
        };
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null || mimeType.isBlank() ? "application/octet-stream" : mimeType.trim();
    }

    private String normalizeCaption(String caption) {
        return caption == null || caption.isBlank() ? null : caption.trim();
    }

    private String toWhatsappPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Conversation phone is required to send WhatsApp media");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 12 || digits.length() > 15) {
            throw new IllegalArgumentException("Conversation phone is not valid for WhatsApp media sending");
        }
        return digits;
    }
}
