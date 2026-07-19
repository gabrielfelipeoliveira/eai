package com.eai.application.user;

import com.eai.application.auth.RefreshTokenRepository;
import com.eai.application.security.PasswordHasher;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final UserService service = new UserService(userRepository, passwordHasher, companyService, storeService, refreshTokenRepository);

    @DisplayName("Aceita admin sem empresa ou loja")
    @Test
    void acceptsAdminWithoutCompanyOrStore() {
        arrangeSave();

        User user = service.createUser(command(UserRole.ADMIN, null, null));

        assertThat(user.getCompanyId()).isNull();
        assertThat(user.getStoreId()).isNull();
    }

    @DisplayName("Aceita gerente com empresa e sem loja")
    @Test
    void acceptsManagerWithCompanyAndWithoutStore() {
        arrangeActiveCompany();
        arrangeSave();

        User user = service.createUser(command(UserRole.MANAGER, COMPANY_ID, null));

        assertThat(user.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(user.getStoreId()).isNull();
    }

    @DisplayName("Rejeita gerente com loja")
    @Test
    void rejectsManagerWithStore() {
        arrangeActiveCompany();

        assertThatThrownBy(() -> service.createUser(command(UserRole.MANAGER, COMPANY_ID, STORE_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MANAGER");
    }

    @DisplayName("Exige loja para papeis operacionais")
    @Test
    void requiresStoreForOperationalRoles() {
        arrangeActiveCompany();

        assertThatThrownBy(() -> service.createUser(command(UserRole.SELLER, COMPANY_ID, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("storeId");
    }

    @DisplayName("Aceita papel operacional com empresa e loja ativas")
    @Test
    void acceptsOperationalRoleWithActiveCompanyAndStore() {
        arrangeActiveCompany();
        when(storeService.findRequired(STORE_ID)).thenReturn(store(TenantStatus.ACTIVE));
        arrangeSave();

        User user = service.createUser(command(UserRole.SELLER, COMPANY_ID, STORE_ID));

        assertThat(user.getStoreId()).isEqualTo(STORE_ID);
    }

    @DisplayName("Desativacao de usuario marca inativo e revoga sessoes")
    @Test
    void deactivateUserMarksUserInactiveAndRevokesSessions() {
        User user = user(UserRole.SELLER);
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User deactivatedUser = service.deactivateUser(user.getId());

        assertThat(deactivatedUser.isActive()).isFalse();
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    private CreateUserCommand command(UserRole role, UUID companyId, UUID storeId) {
        return new CreateUserCommand("User", role.name().toLowerCase() + "@eai.com", "secret123", null, null, companyId, storeId, Set.of(role));
    }

    private void arrangeActiveCompany() {
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
    }

    private void arrangeSave() {
        when(passwordHasher.hash("secret123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Company company(TenantStatus status) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new Company(COMPANY_ID, "EAI", status, now, now);
    }

    private Store store(TenantStatus status) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new Store(STORE_ID, COMPANY_ID, "Loja", "00000000000192", null, null, null, null, null, status, now, now);
    }

    private User user(UserRole role) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new User(UUID.randomUUID(), "User", "user@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, com.eai.domain.user.UserStatus.ACTIVE, Set.of(role), now, now);
    }
}
