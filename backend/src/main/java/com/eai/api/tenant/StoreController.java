package com.eai.api.tenant;

import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CreateStoreCommand;
import com.eai.application.tenant.StoreService;
import com.eai.application.tenant.UpdateStoreCommand;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER', 'AUDITOR')")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreResponse> listStores(
            @RequestParam(required = false) UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (companyId != null) {
            return storeService.listStoresByCompany(companyId, authenticatedUser).stream()
                    .map(StoreResponse::fromDomain)
                    .toList();
        }
        return storeService.listStores(authenticatedUser).stream()
                .map(StoreResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    public StoreResponse getStore(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return StoreResponse.fromDomain(storeService.getStore(id, authenticatedUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public StoreResponse createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return StoreResponse.fromDomain(storeService.createStore(new CreateStoreCommand(
                request.companyId(),
                request.name(),
                request.document(),
                request.email(),
                request.phone(),
                request.city(),
                request.state(),
                request.address()
        ), authenticatedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public StoreResponse updateStore(
            @PathVariable UUID id,
            @Valid @RequestBody StoreRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return StoreResponse.fromDomain(storeService.updateStore(id, new UpdateStoreCommand(
                request.companyId(),
                request.name(),
                request.document(),
                request.email(),
                request.phone(),
                request.city(),
                request.state(),
                request.address(),
                request.status()
        ), authenticatedUser));
    }
}
