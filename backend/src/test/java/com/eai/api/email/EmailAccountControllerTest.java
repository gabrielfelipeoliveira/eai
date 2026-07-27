package com.eai.api.email;

import com.eai.application.email.EmailAccountService;
import com.eai.application.email.EmailImportResult;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailAccountControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final AuthenticatedUser USER = new AuthenticatedUser(UUID.randomUUID(), "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));

    private final EmailAccountService emailAccountService = mock(EmailAccountService.class);
    private final EmailAccountController controller = new EmailAccountController(emailAccountService);

    @DisplayName("Lista e busca contas convertendo dominio para resposta sem senha")
    @Test
    void listAndGetAccountsMapDomainToResponse() {
        EmailAccount account = account();
        when(emailAccountService.listAccounts(USER)).thenReturn(List.of(account));
        when(emailAccountService.getAccount(ACCOUNT_ID, USER)).thenReturn(account);

        assertThat(controller.listAccounts(USER).getFirst().id()).isEqualTo(ACCOUNT_ID);
        assertThat(controller.getAccount(ACCOUNT_ID, USER).username()).isEqualTo("contato@eai.com");
    }

    @DisplayName("Cria e atualiza conta repassando comandos ao servico")
    @Test
    void createAndUpdateDelegateCommands() {
        EmailAccount account = account();
        when(emailAccountService.createAccount(any(), eq(USER))).thenReturn(account);
        when(emailAccountService.updateAccount(eq(ACCOUNT_ID), any(), eq(USER))).thenReturn(account);

        EmailAccountResponse created = controller.createAccount(new EmailAccountCreateRequest(
                COMPANY_ID, STORE_ID, "Principal", "imap.eai.com", 993, "contato@eai.com", "secret", null, true, true
        ), USER);
        EmailAccountResponse updated = controller.updateAccount(ACCOUNT_ID, new EmailAccountRequest(
                COMPANY_ID, STORE_ID, "Principal", "imap.eai.com", 993, "contato@eai.com", "", EmailProtocol.IMAP, true, false
        ), USER);

        assertThat(created.protocol()).isEqualTo(EmailProtocol.IMAP);
        assertThat(updated.id()).isEqualTo(ACCOUNT_ID);
        verify(emailAccountService).createAccount(any(), eq(USER));
        verify(emailAccountService).updateAccount(eq(ACCOUNT_ID), any(), eq(USER));
    }

    @DisplayName("Exclui, testa conexao e sincroniza conta por id")
    @Test
    void deleteTestAndSyncDelegateById() {
        when(emailAccountService.testConnection(ACCOUNT_ID, USER)).thenReturn(new EmailImportResult(0, 0, 0, "SUCCESS", "ok"));
        when(emailAccountService.sync(ACCOUNT_ID, USER)).thenReturn(new EmailImportResult(2, 1, 1, "SUCCESS", "sync"));

        controller.deleteAccount(ACCOUNT_ID, USER);
        EmailImportResponse tested = controller.testConnection(ACCOUNT_ID, USER);
        EmailImportResponse synced = controller.sync(ACCOUNT_ID, USER);

        verify(emailAccountService).deleteAccount(ACCOUNT_ID, USER);
        assertThat(tested.status()).isEqualTo("SUCCESS");
        assertThat(synced.messagesRead()).isEqualTo(2);
    }

    private EmailAccount account() {
        return new EmailAccount(
                ACCOUNT_ID,
                COMPANY_ID,
                STORE_ID,
                "Principal",
                "imap.eai.com",
                993,
                "contato@eai.com",
                "encrypted",
                EmailProtocol.IMAP,
                true,
                true,
                null,
                java.time.Instant.now(),
                java.time.Instant.now(),
                null,
                null,
                null
        );
    }
}
