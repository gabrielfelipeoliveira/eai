package com.eai.application.whatsapp;

import java.util.List;

public interface WhatsAppMediaSettings {

    Long maxImageSizeBytes();

    Long maxAudioSizeBytes();

    Long maxDocumentSizeBytes();

    List<String> allowedMimeTypes();
}
