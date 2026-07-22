package com.eai.application.conversation;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.NotFoundException;
import com.eai.application.media.MediaObject;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConversationMediaService {

    private final ConversationService conversationService;
    private final ConversationMessageRepository messageRepository;
    private final MediaStoragePort mediaStorage;

    public ConversationMediaService(
            ConversationService conversationService,
            ConversationMessageRepository messageRepository,
            MediaStoragePort mediaStorage
    ) {
        this.conversationService = conversationService;
        this.messageRepository = messageRepository;
        this.mediaStorage = mediaStorage;
    }

    @Transactional
    public ConversationMediaDownload download(UUID conversationId, UUID messageId, AuthenticatedUser authenticatedUser) {
        Conversation conversation = conversationService.getConversation(conversationId, authenticatedUser);
        ConversationMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Conversation message not found"));
        if (!conversation.getId().equals(message.getConversationId())) {
            throw new NotFoundException("Conversation message not found");
        }
        if (message.getMediaStorageProvider() == null || message.getMediaStorageKey() == null) {
            throw new ApplicationException("CONVERSATION_MESSAGE_MEDIA_NOT_FOUND", "Conversation message has no stored media");
        }
        MediaObject media = mediaStorage.read(message.getMediaStorageProvider(), message.getMediaStorageKey());
        return new ConversationMediaDownload(
                firstNonBlank(message.getMediaFileName(), media.fileName(), "media.bin"),
                firstNonBlank(message.getMediaMimeType(), media.mimeType(), "application/octet-stream"),
                message.getMediaSizeBytes() == null ? media.sizeBytes() : message.getMediaSizeBytes(),
                media.content()
        );
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
