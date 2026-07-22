package com.eai.application.media;

import java.util.UUID;

public record StoreMediaCommand(
        UUID companyId,
        UUID storeId,
        String source,
        String externalMediaId,
        String fileName,
        String mimeType,
        byte[] content,
        String sha256
) {
}
