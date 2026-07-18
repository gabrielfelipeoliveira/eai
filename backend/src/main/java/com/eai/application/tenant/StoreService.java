package com.eai.application.tenant;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final CompanyService companyService;

    public StoreService(StoreRepository storeRepository, CompanyService companyService) {
        this.storeRepository = storeRepository;
        this.companyService = companyService;
    }

    @Transactional(readOnly = true)
    public List<Store> listStores(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return storeRepository.findAll();
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER)) {
            UUID companyId = requireCompany(authenticatedUser);
            if (authenticatedUser.storeId() != null) {
                return storeRepository.findByIdIn(List.of(authenticatedUser.storeId()));
            }
            return storeRepository.findByCompanyId(companyId);
        }
        return storeRepository.findByIdIn(List.of(requireStore(authenticatedUser)));
    }

    @Transactional(readOnly = true)
    public List<Store> listStoresByCompany(UUID companyId, AuthenticatedUser authenticatedUser) {
        assertCanAccessCompany(companyId, authenticatedUser);
        if (!hasRole(authenticatedUser, UserRole.ADMIN) && authenticatedUser.storeId() != null) {
            return storeRepository.findByIdIn(List.of(authenticatedUser.storeId()));
        }
        return storeRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public Store getStore(UUID id, AuthenticatedUser authenticatedUser) {
        Store store = findRequired(id);
        assertCanAccessStore(store, authenticatedUser);
        return store;
    }

    @Transactional
    public Store createStore(CreateStoreCommand command, AuthenticatedUser authenticatedUser) {
        assertCanManageCompany(command.companyId(), authenticatedUser);
        companyService.findRequired(command.companyId());
        String document = normalizeDocument(command.document());
        if (storeRepository.existsByDocument(document)) {
            throw new ConflictException("Store document already registered");
        }
        return storeRepository.save(Store.create(
                command.companyId(),
                command.name(),
                document,
                command.email(),
                command.phone(),
                command.city(),
                command.state(),
                command.address()
        ));
    }

    @Transactional
    public Store updateStore(UUID id, UpdateStoreCommand command, AuthenticatedUser authenticatedUser) {
        Store store = findRequired(id);
        assertCanAccessStore(store, authenticatedUser);
        assertCanManageCompany(command.companyId(), authenticatedUser);
        companyService.findRequired(command.companyId());
        String document = normalizeDocument(command.document());
        if (storeRepository.existsByDocumentAndIdNot(document, id)) {
            throw new ConflictException("Store document already registered");
        }
        store.update(
                command.companyId(),
                command.name(),
                document,
                command.email(),
                command.phone(),
                command.city(),
                command.state(),
                command.address(),
                command.status()
        );
        return storeRepository.save(store);
    }

    public Store findRequired(UUID id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));
    }

    private void assertCanAccessCompany(UUID companyId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (!companyId.equals(requireCompany(authenticatedUser))) {
            throw new ForbiddenException("Access denied for company");
        }
    }

    private void assertCanAccessStore(Store store, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER)
                && store.getCompanyId().equals(requireCompany(authenticatedUser))) {
            if (authenticatedUser.storeId() == null || authenticatedUser.storeId().equals(store.getId())) {
                return;
            }
        }
        if (store.getId().equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for store");
    }

    private void assertCanManageCompany(UUID companyId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(requireCompany(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for company");
    }

    private UUID requireCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() == null) {
            throw new ForbiddenException("User is not linked to a company");
        }
        return authenticatedUser.companyId();
    }

    private UUID requireStore(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.storeId() == null) {
            throw new ForbiddenException("User is not linked to a store");
        }
        return authenticatedUser.storeId();
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }

    private String normalizeDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("document is required");
        }
        return document.trim();
    }
}
