package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.NotFoundException;
import com.eai.application.conversation.ConversationMessageRepository;
import com.eai.application.conversation.ConversationService;
import com.eai.application.conversation.WhatsAppContactRepository;
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
public class WhatsAppTextSenderService {

    private static final Duration FREE_TEXT_WINDOW = Duration.ofHours(24);

    private final ConversationService conversationService;
    private final WhatsAppContactRepository contactRepository;
    private final ConversationMessageRepository messageRepository;
    private final WhatsAppChannelSettings settings;
    private final WhatsAppTextClient textClient;

    public WhatsAppTextSenderService(
            ConversationService conversationService,
            WhatsAppContactRepository contactRepository,
            ConversationMessageRepository messageRepository,
            WhatsAppChannelSettings settings,
            WhatsAppTextClient textClient
    ) {
        this.conversationService = conversationService;
        this.contactRepository = contactRepository;
        this.messageRepository = messageRepository;
        this.settings = settings;
        this.textClient = textClient;
    }

    @Transactional
    public WhatsAppTextSendResult sendText(UUID conversationId, String content, AuthenticatedUser authenticatedUser) {
        if (!settings.templateSendingConfigured()) {
            throw new ApplicationException("WHATSAPP_TEXT_SENDING_NOT_CONFIGURED", "WhatsApp text sending is not configured");
        }
        String message = normalizeContent(content);
        Conversation conversation = conversationService.getConversation(conversationId, authenticatedUser);
        assertWithinFreeTextWindow(conversation.getId());
        WhatsAppContact contact = contactRepository.findById(conversation.getContactId())
                .orElseThrow(() -> new NotFoundException("WhatsApp contact not found"));

        WhatsAppTextProviderResult providerResult = textClient.sendText(toWhatsappPhone(contact.getPhone()), message);
        ConversationMessageStatus status = providerResult.successful()
                ? ConversationMessageStatus.SENT
                : ConversationMessageStatus.FAILED;
        ConversationMessage conversationMessage = ConversationMessage.outbound(
                conversation.getId(),
                ConversationMessageType.TEXT,
                status,
                providerResult.externalMessageId(),
                message,
                providerResult.rawResponse()
        );
        ConversationMessage savedMessage = messageRepository.save(conversationMessage);
        return new WhatsAppTextSendResult(
                conversation.getId(),
                savedMessage.getId(),
                savedMessage.getStatus(),
                savedMessage.getExternalMessageId(),
                savedMessage.getContent(),
                providerResult.rawResponse(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt()
        );
    }

    private void assertWithinFreeTextWindow(UUID conversationId) {
        ConversationMessage latestInbound = messageRepository.findLatestByConversationIdAndDirection(conversationId, ConversationMessageDirection.INBOUND)
                .orElseThrow(() -> new ApplicationException(
                        "WHATSAPP_FREE_TEXT_WINDOW_EXPIRED",
                        "Free text messages require a customer message in the last 24 hours. Use a WhatsApp template."
                ));
        Instant windowStart = Instant.now().minus(FREE_TEXT_WINDOW);
        if (latestInbound.getCreatedAt().isBefore(windowStart)) {
            throw new ApplicationException(
                    "WHATSAPP_FREE_TEXT_WINDOW_EXPIRED",
                    "Free text messages require a customer message in the last 24 hours. Use a WhatsApp template."
            );
        }
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }
        return content.trim();
    }

    private String toWhatsappPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Conversation phone is required to send WhatsApp text");
        }
        String digits = phone.replaceAll("\\D", "");
        return validatePhone(digits);
    }

    private String validatePhone(String phone) {
        if (phone.length() < 12 || phone.length() > 15) {
            throw new IllegalArgumentException("Conversation phone is not valid for WhatsApp text sending");
        }
        return phone;
    }
}
