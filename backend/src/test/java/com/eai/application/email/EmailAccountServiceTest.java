package com.eai.application.email;

import com.eai.application.common.ForbiddenException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreRepository;
import com.eai.application.tenant.StoreService;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailAccountServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
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

    @DisplayName("Lista todas as contas de e-mail para administrador")
    @Test
    void listsAllAccountsForAdmin() {
        EmailAccount account = account();
        when(emailAccountRepository.findAll()).thenReturn(List.of(account));

        List<EmailAccount> accounts = service.listAccounts(authenticatedUser(UserRole.ADMIN, null, null));

        assertThat(accounts).containsExactly(account);
    }

    @DisplayName("Lista contas de e-mail da loja para gerente vinculado a loja")
    @Test
    void listsStoreAccountsForStoreManager() {
        EmailAccount account = account();
        when(emailAccountRepository.findByStoreIdIn(List.of(STORE_ID))).thenReturn(List.of(account));

        List<EmailAccount> accounts = service.listAccounts(authenticatedUser(UserRole.MANAGER, COMPANY_ID, STORE_ID));

        assertThat(accounts).containsExactly(account);
    }

    @DisplayName("Cria conta de e-mail criptografando senha e validando loja")
    @Test
    void createsAccountEncryptingPasswordAndValidatingStore() {
        arrangeActiveTenant();
        when(encryptionService.encrypt("plain-password")).thenReturn("encrypted-password");
        when(emailAccountRepository.save(any(EmailAccount.class))).thenAnswer(invocation -> invocation.getArgument(0, EmailAccount.class));

        EmailAccount createdAccount = service.createAccount(
                new CreateEmailAccountCommand(COMPANY_ID, STORE_ID, "Leads", "imap.example.com", 993, "leads@example.com", "plain-password", null, true, true),
                authenticatedUser(UserRole.MANAGER, COMPANY_ID, null)
        );

        assertThat(createdAccount.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(createdAccount.getStoreId()).isEqualTo(STORE_ID);
        assertThat(createdAccount.getEncryptedPassword()).isEqualTo("encrypted-password");
        assertThat(createdAccount.getProtocol()).isEqualTo(EmailProtocol.IMAP);
    }

    @DisplayName("Atualiza conta mantendo senha atual quando nova senha esta em branco")
    @Test
    void updatesAccountKeepingCurrentPasswordWhenNewPasswordIsBlank() {
        EmailAccount account = account();
        when(emailAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        arrangeActiveTenant();
        when(emailAccountRepository.save(any(EmailAccount.class))).thenAnswer(invocation -> invocation.getArgument(0, EmailAccount.class));

        EmailAccount updatedAccount = service.updateAccount(
                ACCOUNT_ID,
                new UpdateEmailAccountCommand(COMPANY_ID, STORE_ID, "Leads Atualizado", "imap2.example.com", 995, "novo@example.com", " ", EmailProtocol.IMAP, false, false),
                authenticatedUser(UserRole.ADMIN, null, null)
        );

        assertThat(updatedAccount.getName()).isEqualTo("Leads Atualizado");
        assertThat(updatedAccount.getHost()).isEqualTo("imap2.example.com");
        assertThat(updatedAccount.getEncryptedPassword()).isEqualTo("encrypted-password");
        assertThat(updatedAccount.isUseSsl()).isFalse();
        assertThat(updatedAccount.isActive()).isFalse();
    }

    @DisplayName("Testa conexao com sucesso e registra status da conta")
    @Test
    void testConnectionRecordsSuccess() {
        EmailAccount account = account();
        when(emailAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(encryptionService.decrypt("encrypted-password")).thenReturn("plain-password");

        EmailImportResult result = service.testConnection(ACCOUNT_ID, authenticatedUser(UserRole.ADMIN, null, null));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(account.getLastSyncStatus()).isEqualTo(EmailAccountStatus.SUCCESS);
        assertThat(account.getLastSyncMessage()).isEqualTo("Conexao IMAP testada com sucesso");
        verify(emailReader).testConnection(account, "plain-password");
        verify(emailAccountRepository).save(account);
    }

    @DisplayName("Falha de conexao registra falha e notifica responsaveis")
    @Test
    void testConnectionRecordsFailureAndNotifiesResponsibleUsers() {
        EmailAccount account = account();
        RuntimeException failure = new RuntimeException("IMAP indisponivel");
        when(emailAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(encryptionService.decrypt("encrypted-password")).thenReturn("plain-password");
        doThrow(failure).when(emailReader).testConnection(account, "plain-password");

        assertThatThrownBy(() -> service.testConnection(ACCOUNT_ID, authenticatedUser(UserRole.ADMIN, null, null)))
                .isSameAs(failure);

        assertThat(account.getLastSyncStatus()).isEqualTo(EmailAccountStatus.FAILED);
        assertThat(account.getLastSyncMessage()).isEqualTo("IMAP indisponivel");
        verify(emailAccountRepository).save(account);
        verify(emailAccountFailureNotifier).notifyEmailAccountFailure(account, "Teste de conexao IMAP", failure);
    }

    @DisplayName("Sincroniza conta delegando importacao ao importador")
    @Test
    void syncDelegatesImportToImporter() {
        EmailAccount account = account();
        AuthenticatedUser user = authenticatedUser(UserRole.ADMIN, null, null);
        EmailImportResult expected = new EmailImportResult(2, 1, 0, "SUCCESS", "ok");
        when(emailAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(emailLeadImporter.importAccount(account, USER_ID)).thenReturn(expected);

        EmailImportResult result = service.sync(ACCOUNT_ID, user);

        assertThat(result).isSameAs(expected);
    }

    @DisplayName("Bloqueia vendedor ao listar contas de e-mail")
    @Test
    void blocksSellerWhenListingEmailAccounts() {
        assertThatThrownBy(() -> service.listAccounts(authenticatedUser(UserRole.SELLER, COMPANY_ID, STORE_ID)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    private void arrangeActiveTenant() {
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company());
        when(storeService.findRequired(STORE_ID)).thenReturn(store());
    }

    private AuthenticatedUser authenticatedUser(UserRole role, UUID companyId, UUID storeId) {
        return new AuthenticatedUser(USER_ID, role.name().toLowerCase() + "@eai.com", companyId, storeId, Set.of(role));
    }

    private Company company() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Company(COMPANY_ID, "EAI", TenantStatus.ACTIVE, now, now);
    }

    private Store store() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Store(STORE_ID, COMPANY_ID, "Loja", "00000000000192", null, null, null, null, null, TenantStatus.ACTIVE, now, now);
    }

    private EmailAccount account() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new EmailAccount(
                ACCOUNT_ID,
                COMPANY_ID,
                STORE_ID,
                "Leads",
                "imap.example.com",
                993,
                "leads@example.com",
                "encrypted-password",
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
