package com.eai.application.whatsapp;

public interface WhatsAppMediaClient {

    WhatsAppMediaMetadata fetchMediaMetadata(String mediaId);

    WhatsAppMediaDownload downloadMedia(WhatsAppMediaMetadata metadata);

    WhatsAppMediaUploadResult uploadMedia(String fileName, String mimeType, byte[] content);

    WhatsAppMediaSendResult sendMedia(String phone, WhatsAppOutboundMediaType type, String mediaId, String caption, String fileName);
}
