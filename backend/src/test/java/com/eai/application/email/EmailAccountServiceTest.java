package com.eai.application.email;

import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreRepository;
import com.eai.application.tenant.StoreService;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailAccountServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    private final EmailAccountRepository emailAccountRepository = mock(EmailAccountRepository.class);
    private final EmailReader emailReader = mock(EmailReader.class);
    private final EmailLeadImporter emailLeadImporter = mock(EmailLeadImporter.class);
    private final EncryptionService encryptionService = mock(EncryptionService.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final EmailAccountFailureNotifier emailAccountFailureNotifier = mock(EmailAccountFailureNotifier.class);
    private final EmailAccountService service = new EmailAccountService(
            emailAccountRepository,
            emailReader,
            emailLeadImporter,
            encryptionService,
            companyService,
            storeService,
            storeRepository,
            emailAccountFailureNotifier
    );

    @DisplayName("Falha no teste de conexao marca conta como falha, notifica administradores e relanca excecao")
    @Test
    void testConnectionFailureUpdatesAccountNotifiesAndRethrows() {
        EmailAccount account = account();
        RuntimeException failure = new RuntimeException("IMAP indisponivel");
        AuthenticatedUser admin = new AuthenticatedUser(USER_ID, "admin@eai.com", null, null, Set.of(UserRole.ADMIN));

        when(emailAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(encryptionService.decrypt("encrypted-password")).thenReturn("plain-password");
        org.mockito.Mockito.doThrow(failure).when(emailReader).testConnection(account, "plain-password");

        assertThatThrownBy(() -> service.testConnection(ACCOUNT_ID, admin)).isSameAs(failure);

        assertThat(account.getLastSyncStatus()).isEqualTo(EmailAccountStatus.FAILED);
        assertThat(account.getLastSyncMessage()).isEqualTo("IMAP indisponivel");
        verify(emailAccountRepository).save(account);
        verify(emailAccountFailureNotifier).notifyEmailAccountFailure(account, "Teste de conexao IMAP", failure);
    }

    private EmailAccount account() {
        return new EmailAccount(
                ACCOUNT_ID,
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                "Leads",
                "imap.example.com",
                993,
                "leads@example.com",
                "encrypted-password",
                EmailProtocol.IMAP,
                true,
                true,
                null,
                Instant.parse("2026-07-07T10:00:00Z"),
                Instant.parse("2026-07-07T10:00:00Z"),
                EmailAccountStatus.NEVER_SYNCED,
                null,
                null
        );
    }
}
