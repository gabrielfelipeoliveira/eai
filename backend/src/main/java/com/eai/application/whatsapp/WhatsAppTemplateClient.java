package com.eai.application.whatsapp;

import java.util.List;

public interface WhatsAppTemplateClient {

    WhatsAppTemplateProviderResult sendTemplate(
            String phone,
            String templateName,
            String languageCode,
            List<String> bodyParameters
    );
}
