package com.eai.application.whatsapp;

import java.util.UUID;

public record SendWhatsAppTemplateCommand(UUID templateId, String languageCode) {
}
