package com.eai.application.tenant;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoreServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StoreService service = new StoreService(storeRepository, companyService, userRepository);

    @DisplayName("Lista todas as lojas para administrador")
    @Test
    void listsAllStoresForAdmin() {
        Store store = store(TenantStatus.ACTIVE);
        when(storeRepository.findAll()).thenReturn(List.of(store));

        List<Store> stores = service.listStores(authenticatedUser(UserRole.ADMIN, null, null));

        assertThat(stores).containsExactly(store);
    }

    @DisplayName("Lista lojas da empresa para gerente sem loja")
    @Test
    void listsCompanyStoresForManagerWithoutStore() {
        Store store = store(TenantStatus.ACTIVE);
        when(storeRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(store));

        List<Store> stores = service.listStores(authenticatedUser(UserRole.MANAGER, COMPANY_ID, null));

        assertThat(stores).containsExactly(store);
    }

    @DisplayName("Lista apenas a loja do usuario operacional")
    @Test
    void listsOnlyAuthenticatedStoreForOperationalUser() {
        Store store = store(TenantStatus.ACTIVE);
        when(storeRepository.findByIdIn(List.of(STORE_ID))).thenReturn(List.of(store));

        List<Store> stores = service.listStores(authenticatedUser(UserRole.SELLER, COMPANY_ID, STORE_ID));

        assertThat(stores).containsExactly(store);
    }

    @DisplayName("Cria loja com documento normalizado")
    @Test
    void createsStoreWithNormalizedDocument() {
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(storeRepository.existsByDocument("00000000000192")).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0, Store.class));

        Store store = service.createStore(
                new CreateStoreCommand(COMPANY_ID, "Loja", " 00000000000192 ", null, null, "Sao Paulo", "SP", null),
                authenticatedUser(UserRole.ADMIN, null, null)
        );

        assertThat(store.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(store.getDocument()).isEqualTo("00000000000192");
        assertThat(store.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @DisplayName("Rejeita criacao de loja com documento duplicado")
    @Test
    void rejectsCreationWithDuplicatedDocument() {
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(storeRepository.existsByDocument("00000000000192")).thenReturn(true);

        assertThatThrownBy(() -> service.createStore(
                new CreateStoreCommand(COMPANY_ID, "Loja", "00000000000192", null, null, null, null, null),
                authenticatedUser(UserRole.ADMIN, null, null)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Store document already registered");
    }

    @DisplayName("Bloqueia gerente ao listar lojas de outra empresa")
    @Test
    void blocksManagerListingStoresFromAnotherCompany() {
        assertThatThrownBy(() -> service.listStoresByCompany(OTHER_COMPANY_ID, authenticatedUser(UserRole.MANAGER, COMPANY_ID, null)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @DisplayName("Desativa loja e usuarios ativos vinculados")
    @Test
    void deactivatesStoreAndActiveUsersLinkedToStore() {
        Store store = store(TenantStatus.ACTIVE);
        User seller = user("00000000-0000-0000-0000-000000000301");
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(userRepository.findActiveByStoreId(STORE_ID)).thenReturn(List.of(seller));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0, Store.class));

        Store updated = service.updateStore(
                STORE_ID,
                new UpdateStoreCommand(COMPANY_ID, "Loja", "00000000000192", null, null, null, null, null, TenantStatus.INACTIVE),
                authenticatedUser(UserRole.ADMIN, null, null)
        );

        assertThat(updated.getStatus()).isEqualTo(TenantStatus.INACTIVE);
        assertThat(seller.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(seller);
    }

    private AuthenticatedUser authenticatedUser(UserRole role, UUID companyId, UUID storeId) {
        return new AuthenticatedUser(UUID.randomUUID(), role.name().toLowerCase() + "@eai.com", companyId, storeId, Set.of(role));
    }

    private Company company(TenantStatus status) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new Company(COMPANY_ID, "EAI", status, now, now);
    }

    private Store store(TenantStatus status) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new Store(STORE_ID, COMPANY_ID, "Loja", "00000000000192", null, null, null, null, null, status, now, now);
    }

    private User user(String id) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new User(UUID.fromString(id), "Seller", "seller@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, UserStatus.ACTIVE, Set.of(UserRole.SELLER), now, now);
    }
}
