package com.eai.api.tenant;

import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.StoreService;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoreControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final AuthenticatedUser USER = new AuthenticatedUser(UUID.randomUUID(), "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));

    private final StoreService storeService = mock(StoreService.class);
    private final StoreController controller = new StoreController(storeService);

    @DisplayName("Lista lojas por empresa quando filtro companyId e informado")
    @Test
    void listStoresUsesCompanyFilterWhenPresent() {
        when(storeService.listStoresByCompany(COMPANY_ID, USER)).thenReturn(List.of(store()));

        List<StoreResponse> responses = controller.listStores(COMPANY_ID, USER);

        assertThat(responses).hasSize(1);
        verify(storeService).listStoresByCompany(COMPANY_ID, USER);
    }

    @DisplayName("Lista lojas do escopo do usuario quando nao ha filtro de empresa")
    @Test
    void listStoresUsesUserScopeWithoutCompanyFilter() {
        when(storeService.listStores(USER)).thenReturn(List.of(store()));

        List<StoreResponse> responses = controller.listStores(null, USER);

        assertThat(responses.getFirst().id()).isEqualTo(STORE_ID);
        verify(storeService).listStores(USER);
    }

    @DisplayName("Busca, cria e atualiza loja convertendo requisicoes em comandos")
    @Test
    void getCreateAndUpdateStore() {
        when(storeService.getStore(STORE_ID, USER)).thenReturn(store());
        when(storeService.createStore(any(), eq(USER))).thenReturn(store());
        when(storeService.updateStore(eq(STORE_ID), any(), eq(USER))).thenReturn(store());

        assertThat(controller.getStore(STORE_ID, USER).name()).isEqualTo("Loja Centro");
        assertThat(controller.createStore(new StoreCreateRequest(
                COMPANY_ID, "Loja Centro", "123", "loja@eai.com", "11999990000", "Sao Paulo", "SP", "Rua 1"
        ), USER).document()).isEqualTo("123");
        assertThat(controller.updateStore(STORE_ID, new StoreRequest(
                COMPANY_ID, "Loja Centro", "123", "loja@eai.com", "11999990000", "Sao Paulo", "SP", "Rua 1", TenantStatus.ACTIVE
        ), USER).status()).isEqualTo(TenantStatus.ACTIVE);
    }

    private Store store() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Store(STORE_ID, COMPANY_ID, "Loja Centro", "123", "loja@eai.com", "11999990000", "Sao Paulo", "SP", "Rua 1", TenantStatus.ACTIVE, now, now);
    }
}
