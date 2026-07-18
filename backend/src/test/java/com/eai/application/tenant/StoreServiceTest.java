package com.eai.application.tenant;

import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoreServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StoreService service = new StoreService(storeRepository, companyService, userRepository);

    @Test
    void deactivatesStoreAndActiveUsersLinkedToStore() {
        Store store = store(TenantStatus.ACTIVE);
        User seller = user("00000000-0000-0000-0000-000000000301");
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(userRepository.findActiveByStoreId(STORE_ID)).thenReturn(List.of(seller));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Store updated = service.updateStore(STORE_ID, new UpdateStoreCommand(
                COMPANY_ID, "Loja", "00000000000192", null, null, null, null, null, TenantStatus.INACTIVE
        ), admin());

        assertThat(updated.getStatus()).isEqualTo(TenantStatus.INACTIVE);
        assertThat(seller.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(seller);
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), "admin@eai.com", null, null, Set.of(UserRole.ADMIN));
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
