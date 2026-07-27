package com.eai.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppCloudApiPropertiesAdditionalTest {

    @DisplayName("Indica configuracoes de webhook persistencia e envio de templates")
    @Test
    void reportsConfiguredCapabilities() {
        WhatsAppCloudApiProperties configured = new WhatsAppCloudApiProperties(
                "phone-number",
                "business",
                "token",
                "secret",
                "verify",
                "v20.0",
                "company",
                "store"
        );
        WhatsAppCloudApiProperties blank = new WhatsAppCloudApiProperties(" ", null, "", null, " ", null, "company", " ");

        assertThat(configured.webhookConfigured()).isTrue();
        assertThat(configured.inboundPersistenceConfigured()).isTrue();
        assertThat(configured.templateSendingConfigured()).isTrue();
        assertThat(blank.webhookConfigured()).isFalse();
        assertThat(blank.inboundPersistenceConfigured()).isFalse();
        assertThat(blank.templateSendingConfigured()).isFalse();
    }
}
