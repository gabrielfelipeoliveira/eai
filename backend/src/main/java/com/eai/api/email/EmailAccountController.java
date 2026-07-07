package com.eai.api.email;

import com.eai.application.email.CreateEmailAccountCommand;
import com.eai.application.email.EmailAccountService;
import com.eai.application.email.UpdateEmailAccountCommand;
import com.eai.application.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/email-accounts")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    public EmailAccountController(EmailAccountService emailAccountService) {
        this.emailAccountService = emailAccountService;
    }

    @GetMapping
    public List<EmailAccountResponse> listAccounts(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return emailAccountService.listAccounts(authenticatedUser).stream()
                .map(EmailAccountResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    public EmailAccountResponse getAccount(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return EmailAccountResponse.fromDomain(emailAccountService.getAccount(id, authenticatedUser));
    }

    @PostMapping
    public EmailAccountResponse createAccount(
            @Valid @RequestBody EmailAccountCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return EmailAccountResponse.fromDomain(emailAccountService.createAccount(new CreateEmailAccountCommand(
                request.companyId(),
                request.storeId(),
                request.name(),
                request.host(),
                request.port(),
                request.username(),
                request.password(),
                request.protocol(),
                request.useSsl(),
                request.active()
        ), authenticatedUser));
    }

    @PutMapping("/{id}")
    public EmailAccountResponse updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody EmailAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return EmailAccountResponse.fromDomain(emailAccountService.updateAccount(id, new UpdateEmailAccountCommand(
                request.companyId(),
                request.storeId(),
                request.name(),
                request.host(),
                request.port(),
                request.username(),
                request.password(),
                request.protocol(),
                request.useSsl(),
                request.active()
        ), authenticatedUser));
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        emailAccountService.deleteAccount(id, authenticatedUser);
    }

    @PostMapping("/{id}/test")
    public EmailImportResponse testConnection(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return EmailImportResponse.fromResult(emailAccountService.testConnection(id, authenticatedUser));
    }

    @PostMapping("/{id}/sync")
    public EmailImportResponse sync(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return EmailImportResponse.fromResult(emailAccountService.sync(id, authenticatedUser));
    }
}
