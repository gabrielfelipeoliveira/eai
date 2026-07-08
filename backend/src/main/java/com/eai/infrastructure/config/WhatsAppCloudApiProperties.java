package com.eai.infrastructure.config;

import com.eai.application.whatsapp.WhatsAppChannelSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eai.whatsapp.cloud-api")
public record WhatsAppCloudApiProperties(
        String phoneNumberId,
        String businessAccountId,
        String accessToken,
        String appSecret,
        String verifyToken,
        String companyId,
        String storeId
) implements WhatsAppChannelSettings {

    @Override
    public boolean webhookConfigured() {
        return verifyToken != null && !verifyToken.isBlank();
    }

    @Override
    public boolean inboundPersistenceConfigured() {
        return companyId != null && !companyId.isBlank() && storeId != null && !storeId.isBlank();
    }
}
