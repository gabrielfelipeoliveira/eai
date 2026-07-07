package com.eai.api.message;

import com.eai.application.message.CreateMessageTemplateCommand;
import com.eai.application.message.MessageTemplateService;
import com.eai.application.message.UpdateMessageTemplateCommand;
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
@RequestMapping("/api/templates")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
public class MessageTemplateController {

    private final MessageTemplateService templateService;

    public MessageTemplateController(MessageTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<MessageTemplateResponse> listTemplates(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return templateService.listTemplates(authenticatedUser).stream()
                .map(MessageTemplateResponse::fromDomain)
                .toList();
    }

    @GetMapping("/active")
    public List<MessageTemplateResponse> listActiveTemplates(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return templateService.listActiveTemplates(authenticatedUser).stream()
                .map(MessageTemplateResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    public MessageTemplateResponse getTemplate(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return MessageTemplateResponse.fromDomain(templateService.getTemplate(id, authenticatedUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public MessageTemplateResponse createTemplate(@Valid @RequestBody MessageTemplateRequest request, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return MessageTemplateResponse.fromDomain(templateService.createTemplate(new CreateMessageTemplateCommand(
                request.companyId(),
                request.storeId(),
                request.name(),
                request.type(),
                request.content(),
                request.active()
        ), authenticatedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public MessageTemplateResponse updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody MessageTemplateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return MessageTemplateResponse.fromDomain(templateService.updateTemplate(id, new UpdateMessageTemplateCommand(
                request.companyId(),
                request.storeId(),
                request.name(),
                request.type(),
                request.content(),
                request.active()
        ), authenticatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteTemplate(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        templateService.deleteTemplate(id, authenticatedUser);
    }
}
