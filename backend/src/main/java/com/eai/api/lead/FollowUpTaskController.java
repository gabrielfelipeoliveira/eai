package com.eai.api.lead;

import com.eai.application.lead.CreateFollowUpTaskCommand;
import com.eai.application.lead.FollowUpTaskService;
import com.eai.application.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
public class FollowUpTaskController {

    private final FollowUpTaskService service;

    public FollowUpTaskController(FollowUpTaskService service) {
        this.service = service;
    }

    @PostMapping("/leads/{id}/follow-ups")
    public FollowUpTaskResponse create(
            @PathVariable UUID id,
            @Valid @RequestBody FollowUpTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return FollowUpTaskResponse.fromDomain(service.create(id, new CreateFollowUpTaskCommand(
                request.userId(),
                request.title(),
                request.description(),
                request.dueAt()
        ), authenticatedUser));
    }

    @GetMapping("/leads/{id}/follow-ups")
    public List<FollowUpTaskResponse> listByLead(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return service.listByLead(id, authenticatedUser).stream()
                .map(FollowUpTaskResponse::fromDomain)
                .toList();
    }

    @GetMapping("/follow-ups")
    public List<FollowUpTaskResponse> list(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return service.list(authenticatedUser).stream()
                .map(FollowUpTaskResponse::fromDomain)
                .toList();
    }

    @GetMapping("/follow-ups/my")
    public List<FollowUpTaskResponse> listMy(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return service.listMy(authenticatedUser).stream()
                .map(FollowUpTaskResponse::fromDomain)
                .toList();
    }

    @PatchMapping("/follow-ups/{id}/complete")
    public FollowUpTaskResponse complete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return FollowUpTaskResponse.fromDomain(service.complete(id, authenticatedUser));
    }

    @PatchMapping("/follow-ups/{id}/cancel")
    public FollowUpTaskResponse cancel(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return FollowUpTaskResponse.fromDomain(service.cancel(id, authenticatedUser));
    }
}
