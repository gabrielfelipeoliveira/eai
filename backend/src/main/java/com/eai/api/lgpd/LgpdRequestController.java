package com.eai.api.lgpd;

import com.eai.application.lgpd.CreateLgpdRequestCommand;
import com.eai.application.lgpd.LgpdRequestSearchCriteria;
import com.eai.application.lgpd.LgpdRequestService;
import com.eai.application.lgpd.RegisterLgpdRequestActionCommand;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.lgpd.LgpdRequestStatus;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/lgpd-requests")
@PreAuthorize("hasRole('ADMIN')")
public class LgpdRequestController {

    private final LgpdRequestService service;

    public LgpdRequestController(LgpdRequestService service) {
        this.service = service;
    }

    @PostMapping
    public LgpdRequestResponse create(
            @Valid @RequestBody LgpdRequestCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LgpdRequestResponse.fromDomain(service.create(new CreateLgpdRequestCommand(
                request.companyId(),
                request.storeId(),
                request.leadId(),
                request.dataSubjectName(),
                request.dataSubjectPhone(),
                request.dataSubjectEmail(),
                request.requestType(),
                request.description()
        ), authenticatedUser));
    }

    @GetMapping
    public LgpdPageResponse<LgpdRequestResponse> list(
            @RequestParam(required = false) LgpdRequestStatus status,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID leadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LgpdPageResponse.from(service.list(
                new LgpdRequestSearchCriteria(status, companyId, storeId, leadId),
                page,
                size,
                authenticatedUser
        ), LgpdRequestResponse::fromDomain);
    }

    @GetMapping("/{id}")
    public LgpdRequestResponse get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return LgpdRequestResponse.fromDetails(service.get(id, authenticatedUser));
    }

    @PostMapping("/{id}/actions")
    public LgpdRequestResponse registerAction(
            @PathVariable UUID id,
            @Valid @RequestBody LgpdRequestActionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LgpdRequestResponse.fromDetails(service.registerAction(id, new RegisterLgpdRequestActionCommand(
                request.actionType(),
                request.resolution(),
                request.finalStatus()
        ), authenticatedUser));
    }
}
