package com.eai.application.user;

import com.eai.application.auth.RefreshTokenRepository;
import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.security.AuthenticatedUser;
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

class UserServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID OTHER_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000202");

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

    @DisplayName("Lista todos os usuarios para administrador")
    @Test
    void listsAllUsersForAdmin() {
        User user = user(UserRole.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = service.listUsers(authenticatedUser(UserRole.ADMIN, null, null));

        assertThat(users).containsExactly(user);
    }

    @DisplayName("Lista usuarios da loja para gerente vinculado a loja")
    @Test
    void listsStoreUsersForStoreManager() {
        User user = user(UserRole.SELLER);
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of(user));

        List<User> users = service.listUsers(authenticatedUser(UserRole.MANAGER, COMPANY_ID, STORE_ID));

        assertThat(users).containsExactly(user);
    }

    @DisplayName("Bloqueia acesso de gerente a usuario de outra loja")
    @Test
    void blocksManagerAccessToUserFromAnotherStore() {
        User user = user(UserRole.SELLER);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getUser(user.getId(), authenticatedUser(UserRole.MANAGER, COMPANY_ID, OTHER_STORE_ID)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @DisplayName("Atualiza usuario normalizando email e senha quando informada")
    @Test
    void updatesUserNormalizingEmailAndPasswordWhenProvided() {
        User user = user(UserRole.SELLER);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("novo@eai.com", user.getId())).thenReturn(false);
        arrangeActiveCompany();
        when(storeService.findRequired(STORE_ID)).thenReturn(store(TenantStatus.ACTIVE));
        when(passwordHasher.hash("nova-senha")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

        User updatedUser = service.updateUser(user.getId(), new UpdateUserCommand(
                "Novo Nome",
                " Novo@EAI.COM ",
                "nova-senha",
                "11999999999",
                "Consultor",
                COMPANY_ID,
                STORE_ID,
                Set.of(UserRole.SELLER)
        ));

        assertThat(updatedUser.getName()).isEqualTo("Novo Nome");
        assertThat(updatedUser.getEmail()).isEqualTo("novo@eai.com");
        assertThat(updatedUser.getPasswordHash()).isEqualTo("new-hash");
    }

    @DisplayName("Rejeita criacao com email ja cadastrado")
    @Test
    void rejectsCreationWithDuplicatedEmail() {
        when(userRepository.existsByEmail("seller@eai.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(command(UserRole.SELLER, COMPANY_ID, STORE_ID)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already registered");
    }

    @DisplayName("Desativacao de usuario marca inativo e revoga sessoes")
    @Test
    void deactivateUserMarksUserInactiveAndRevokesSessions() {
        User user = user(UserRole.SELLER);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
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

    private AuthenticatedUser authenticatedUser(UserRole role, UUID companyId, UUID storeId) {
        return new AuthenticatedUser(UUID.randomUUID(), role.name().toLowerCase() + "@eai.com", companyId, storeId, Set.of(role));
    }
}
