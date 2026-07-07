package com.eai.application.email;

import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreRepository;
import com.eai.application.tenant.StoreService;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final EmailReader emailReader;
    private final EmailLeadImporter emailLeadImporter;
    private final EncryptionService encryptionService;
    private final CompanyService companyService;
    private final StoreService storeService;
    private final StoreRepository storeRepository;

    public EmailAccountService(
            EmailAccountRepository emailAccountRepository,
            EmailReader emailReader,
            EmailLeadImporter emailLeadImporter,
            EncryptionService encryptionService,
            CompanyService companyService,
            StoreService storeService,
            StoreRepository storeRepository
    ) {
        this.emailAccountRepository = emailAccountRepository;
        this.emailReader = emailReader;
        this.emailLeadImporter = emailLeadImporter;
        this.encryptionService = encryptionService;
        this.companyService = companyService;
        this.storeService = storeService;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<EmailAccount> listAccounts(AuthenticatedUser authenticatedUser) {
        assertCanManageEmailAccounts(authenticatedUser);
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return emailAccountRepository.findAll();
        }
        if (authenticatedUser.storeId() != null) {
            return emailAccountRepository.findByStoreIdIn(List.of(authenticatedUser.storeId()));
        }
        return emailAccountRepository.findByStoreIdIn(
                storeRepository.findByCompanyId(requireCompany(authenticatedUser)).stream()
                        .map(Store::getId)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public EmailAccount getAccount(UUID id, AuthenticatedUser authenticatedUser) {
        EmailAccount account = findRequired(id);
        assertCanAccessAccount(account, authenticatedUser);
        return account;
    }

    @Transactional
    public EmailAccount createAccount(CreateEmailAccountCommand command, AuthenticatedUser authenticatedUser) {
        assertCanUseTenant(command.companyId(), command.storeId(), authenticatedUser);
        validateTenant(command.companyId(), command.storeId());
        String encryptedPassword = encryptionService.encrypt(command.password());
        return emailAccountRepository.save(EmailAccount.create(
                command.companyId(),
                command.storeId(),
                command.name(),
                command.host(),
                command.port(),
                command.username(),
                encryptedPassword,
                command.protocol() == null ? EmailProtocol.IMAP : command.protocol(),
                command.useSsl(),
                command.active()
        ));
    }

    @Transactional
    public EmailAccount updateAccount(UUID id, UpdateEmailAccountCommand command, AuthenticatedUser authenticatedUser) {
        EmailAccount account = getAccount(id, authenticatedUser);
        assertCanUseTenant(command.companyId(), command.storeId(), authenticatedUser);
        validateTenant(command.companyId(), command.storeId());
        String encryptedPassword = command.password() == null || command.password().isBlank()
                ? account.getEncryptedPassword()
                : encryptionService.encrypt(command.password());
        account.update(
                command.companyId(),
                command.storeId(),
                command.name(),
                command.host(),
                command.port(),
                command.username(),
                encryptedPassword,
                command.protocol() == null ? EmailProtocol.IMAP : command.protocol(),
                command.useSsl(),
                command.active()
        );
        return emailAccountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(UUID id, AuthenticatedUser authenticatedUser) {
        EmailAccount account = getAccount(id, authenticatedUser);
        emailAccountRepository.deleteById(account.getId());
    }

    @Transactional
    public EmailImportResult testConnection(UUID id, AuthenticatedUser authenticatedUser) {
        EmailAccount account = getAccount(id, authenticatedUser);
        try {
            emailReader.testConnection(account, encryptionService.decrypt(account.getEncryptedPassword()));
            account.recordSuccess(account.getLastReadAt(), "Conexao IMAP testada com sucesso");
            emailAccountRepository.save(account);
            return new EmailImportResult(0, 0, 0, "SUCCESS", "Conexao IMAP testada com sucesso");
        } catch (RuntimeException exception) {
            account.recordFailure(exception.getMessage());
            emailAccountRepository.save(account);
            throw exception;
        }
    }

    @Transactional
    public EmailImportResult sync(UUID id, AuthenticatedUser authenticatedUser) {
        EmailAccount account = getAccount(id, authenticatedUser);
        return emailLeadImporter.importAccount(account, authenticatedUser.id());
    }

    public EmailAccount findRequired(UUID id) {
        return emailAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Email account not found"));
    }

    private void validateTenant(UUID companyId, UUID storeId) {
        if (companyId == null || storeId == null) {
            throw new IllegalArgumentException("companyId and storeId are required");
        }
        companyService.findRequired(companyId);
        Store store = storeService.findRequired(storeId);
        if (!store.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
    }

    private void assertCanAccessAccount(EmailAccount account, AuthenticatedUser authenticatedUser) {
        assertCanManageEmailAccounts(authenticatedUser);
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (!account.getCompanyId().equals(requireCompany(authenticatedUser))) {
            throw new ForbiddenException("Access denied for email account");
        }
        if (authenticatedUser.storeId() == null || account.getStoreId().equals(authenticatedUser.storeId())) {
            return;
        }
        throw new ForbiddenException("Access denied for email account");
    }

    private void assertCanUseTenant(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        assertCanManageEmailAccounts(authenticatedUser);
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (companyId.equals(requireCompany(authenticatedUser)) && (authenticatedUser.storeId() == null || storeId.equals(authenticatedUser.storeId()))) {
            return;
        }
        throw new ForbiddenException("Access denied for email account tenant");
    }

    private void assertCanManageEmailAccounts(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN) || hasRole(authenticatedUser, UserRole.MANAGER)) {
            return;
        }
        throw new ForbiddenException("Access denied for email accounts");
    }

    private UUID requireCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() == null) {
            throw new ForbiddenException("User is not linked to a company");
        }
        return authenticatedUser.companyId();
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }
}
