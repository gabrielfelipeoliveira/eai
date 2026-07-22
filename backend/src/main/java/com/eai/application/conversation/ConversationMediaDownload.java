package com.eai.application.conversation;

public record ConversationMediaDownload(
        String fileName,
        String mimeType,
        long sizeBytes,
        byte[] content
) {
}
