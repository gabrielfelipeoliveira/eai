package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WhatsAppMediaValidatorTest {

    private final WhatsAppMediaSettings settings = mock(WhatsAppMediaSettings.class);
    private final WhatsAppMediaValidator validator = new WhatsAppMediaValidator(settings);

    @DisplayName("Aceita imagem dentro do limite padrao do WhatsApp")
    @Test
    void acceptsImageWithinDefaultWhatsAppLimit() {
        validator.validateUpload("image/jpeg", 5L * 1024 * 1024);
    }

    @DisplayName("Rejeita imagem acima do limite padrao do WhatsApp")
    @Test
    void rejectsImageAboveDefaultWhatsAppLimit() {
        assertThatThrownBy(() -> validator.validateUpload("image/png", 5L * 1024 * 1024 + 1))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file exceeds the configured size limit");
    }

    @DisplayName("Rejeita MIME type fora da lista permitida")
    @Test
    void rejectsMimeTypeOutsideAllowedList() {
        assertThatThrownBy(() -> validator.validateUpload("image/gif", 10))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media MIME type is not supported");
    }

    @DisplayName("Rejeita arquivo vazio")
    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> validator.validateUpload("image/jpeg", 0))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file is empty");
    }

    @DisplayName("Respeita limite customizado por ambiente")
    @Test
    void respectsEnvironmentSpecificLimit() {
        when(settings.maxDocumentSizeBytes()).thenReturn(2L);
        when(settings.allowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThatThrownBy(() -> validator.validateUpload("application/pdf", 3))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Media file exceeds the configured size limit");
    }
}
