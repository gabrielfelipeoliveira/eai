package com.eai.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityCorsPropertiesTest {

    @DisplayName("Mantem origens CORS configuradas para o ambiente")
    @Test
    void keepsConfiguredCorsOrigins() {
        SecurityCorsProperties properties = new SecurityCorsProperties(
                List.of("https://app.eai.local"),
                List.of("GET", "POST"),
                List.of("Authorization", "Content-Type")
        );

        assertThat(properties.effectiveAllowedOrigins()).containsExactly("https://app.eai.local");
    }

    @DisplayName("Rejeita configuracao CORS sem origens permitidas")
    @Test
    void rejectsBlankCorsOrigins() {
        SecurityCorsProperties properties = new SecurityCorsProperties(
                List.of(" "),
                List.of("GET"),
                List.of("Authorization")
        );

        assertThatThrownBy(properties::effectiveAllowedOrigins)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("EAI_CORS_ALLOWED_ORIGINS is required");
    }
}
