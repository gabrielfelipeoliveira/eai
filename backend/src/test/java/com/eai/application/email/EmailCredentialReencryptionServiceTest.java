package com.eai.application.email;

import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class EmailCredentialReencryptionServiceTest {

    private final EmailAccountRepository emailAccountRepository = mock(EmailAccountRepository.class);
    private final EncryptionService encryptionService = mock(EncryptionService.class);
    private final EmailCredentialReencryptionService service =
            new EmailCredentialReencryptionService(emailAccountRepository, encryptionService);

    @DisplayName("Recriptografa apenas credenciais IMAP legadas ou com chave anterior")
    @Test
    void reencryptsOnlyCredentialsThatRequireMigration() {
        EmailAccount current = account("encrypted-current");
        EmailAccount legacy = account("legacy-base64");

        when(emailAccountRepository.findAll()).thenReturn(List.of(current, legacy));
        when(encryptionService.requiresReencryption("encrypted-current")).thenReturn(false);
        when(encryptionService.requiresReencryption("legacy-base64")).thenReturn(true);
        when(encryptionService.decrypt("legacy-base64")).thenReturn("senha-legada");
        when(encryptionService.encrypt("senha-legada")).thenReturn("encrypted-new");

        EmailCredentialReencryptionResult result = service.reencryptAll();

        assertThat(result).isEqualTo(new EmailCredentialReencryptionResult(2, 1, 1, 0));
        assertThat(current.getEncryptedPassword()).isEqualTo("encrypted-current");
        assertThat(legacy.getEncryptedPassword()).isEqualTo("encrypted-new");
        verify(emailAccountRepository, never()).save(current);
        verify(emailAccountRepository).save(legacy);
    }

    @DisplayName("Preserva credencial IMAP original quando migracao falha parcialmente")
    @Test
    void preservesOriginalCredentialWhenPartialMigrationFails() {
        EmailAccount broken = account("encrypted-broken");

        when(emailAccountRepository.findAll()).thenReturn(List.of(broken));
        when(encryptionService.requiresReencryption("encrypted-broken")).thenReturn(true);
        when(encryptionService.decrypt("encrypted-broken")).thenThrow(new IllegalArgumentException("payload invalido senha-super-secreta"));

        EmailCredentialReencryptionResult result = service.reencryptAll();

        assertThat(result).isEqualTo(new EmailCredentialReencryptionResult(1, 0, 0, 1));
        assertThat(broken.getEncryptedPassword()).isEqualTo("encrypted-broken");
        verify(emailAccountRepository, never()).save(broken);
    }

    private EmailAccount account(String encryptedPassword) {
        Instant now = Instant.parse("2026-07-22T10:00:00Z");
        return new EmailAccount(
                UUID.randomUUID(),
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                "Leads",
                "imap.example.com",
                993,
                "leads@example.com",
                encryptedPassword,
                EmailProtocol.IMAP,
                true,
                true,
                null,
                now,
                now,
                EmailAccountStatus.NEVER_SYNCED,
                null,
                null
        );
    }
}
