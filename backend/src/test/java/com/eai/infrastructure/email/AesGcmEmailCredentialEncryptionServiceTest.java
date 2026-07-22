package com.eai.infrastructure.email;

import com.eai.infrastructure.config.EmailCredentialEncryptionProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmEmailCredentialEncryptionServiceTest {

    private final AesGcmEmailCredentialEncryptionService service =
            new AesGcmEmailCredentialEncryptionService(new EmailCredentialEncryptionProperties("segredo-forte-de-teste"));

    @DisplayName("Criptografa credencial IMAP em formato versionado e descriptografa para o valor original")
    @Test
    void encryptsCredentialWithVersionedFormatAndDecryptsOriginalValue() {
        String encrypted = service.encrypt("senha-imap-secreta");

        assertThat(encrypted).startsWith("v1:");
        assertThat(encrypted).doesNotContain("senha-imap-secreta");
        assertThat(service.decrypt(encrypted)).isEqualTo("senha-imap-secreta");
    }

    @DisplayName("Gera textos cifrados diferentes para a mesma credencial IMAP")
    @Test
    void encryptsSameCredentialWithDifferentCipherTexts() {
        String first = service.encrypt("senha-imap-secreta");
        String second = service.encrypt("senha-imap-secreta");

        assertThat(first).isNotEqualTo(second);
        assertThat(service.decrypt(first)).isEqualTo("senha-imap-secreta");
        assertThat(service.decrypt(second)).isEqualTo("senha-imap-secreta");
    }

    @DisplayName("Mantem leitura de credenciais IMAP legadas em Base64")
    @Test
    void decryptsLegacyBase64Credential() {
        String legacy = Base64.getEncoder().encodeToString("senha-legada".getBytes(StandardCharsets.UTF_8));

        assertThat(service.decrypt(legacy)).isEqualTo("senha-legada");
    }

    @DisplayName("Rejeita credencial IMAP criptografada com chave diferente")
    @Test
    void rejectsCredentialEncryptedWithDifferentSecret() {
        AesGcmEmailCredentialEncryptionService otherService =
                new AesGcmEmailCredentialEncryptionService(new EmailCredentialEncryptionProperties("outro-segredo"));
        String encrypted = otherService.encrypt("senha-imap-secreta");

        assertThatThrownBy(() -> service.decrypt(encrypted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid encrypted email credential");
    }

    @DisplayName("Rejeita senha IMAP vazia ao criptografar")
    @Test
    void rejectsBlankPasswordWhenEncrypting() {
        assertThatThrownBy(() -> service.encrypt(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password required");
    }

    @DisplayName("Rejeita inicializacao sem segredo de criptografia IMAP")
    @Test
    void rejectsBlankEncryptionSecret() {
        assertThatThrownBy(() -> new AesGcmEmailCredentialEncryptionService(
                new EmailCredentialEncryptionProperties(" ")
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("EAI_EMAIL_CREDENTIALS_SECRET is required");
    }
}
