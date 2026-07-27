package com.eai.application.user;

import com.eai.application.auth.RefreshTokenRepository;
import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.security.PasswordHasher;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
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

class UserServiceAdditionalTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID OTHER_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final Instant NOW = Instant.parse("2026-07-27T10:00:00Z");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final UserService service = new UserService(userRepository, passwordHasher, companyService, storeService, refreshTokenRepository);

    @DisplayName("Busca usuario por email normalizado")
    @Test
    void findByEmailNormalizesEmail() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findByEmail("seller@eai.com")).thenReturn(Optional.of(user));

        User result = service.getUserByEmail(" Seller@EAI.COM ");

        assertThat(result).isSameAs(user);
    }

    @DisplayName("Busca por email vazio e usuario inexistente falham")
    @Test
    void getByEmailRejectsBlankAndMissingUser() {
        assertThatThrownBy(() -> service.getUserByEmail(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");

        when(userRepository.findByEmail("missing@eai.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUserByEmail("missing@eai.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("Lista usuarios por empresa para gerente sem loja")
    @Test
    void managerWithoutStoreListsCompanyUsers() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(user));

        List<User> users = service.listUsers(authenticated(UserRole.MANAGER, COMPANY_ID, null));

        assertThat(users).containsExactly(user);
    }

    @DisplayName("Vendedor lista apenas usuarios da sua loja")
    @Test
    void sellerListsStoreUsers() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of(user));

        List<User> users = service.listUsers(authenticated(UserRole.SELLER, COMPANY_ID, STORE_ID));

        assertThat(users).containsExactly(user);
    }

    @DisplayName("Admin acessa qualquer usuario e gerente acessa usuario da empresa")
    @Test
    void accessRulesAllowAdminAndCompanyManager() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(service.getUser(user.getId(), authenticated(UserRole.ADMIN, null, null))).isSameAs(user);
        assertThat(service.getUser(user.getId(), authenticated(UserRole.MANAGER, COMPANY_ID, null))).isSameAs(user);
    }

    @DisplayName("Usuario operacional acessa usuario da propria loja")
    @Test
    void storeUserAccessesUsersFromOwnStore() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = service.getUser(user.getId(), authenticated(UserRole.SELLER, COMPANY_ID, STORE_ID));

        assertThat(result).isSameAs(user);
    }

    @DisplayName("Atualizacao rejeita email duplicado")
    @Test
    void updateRejectsDuplicatedEmail() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("duplicado@eai.com", user.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(user.getId(), updateCommand("duplicado@eai.com", Set.of(UserRole.SELLER), COMPANY_ID, STORE_ID)))
                .isInstanceOf(ConflictException.class);
    }

    @DisplayName("Atualizacao sem senha preserva hash atual")
    @Test
    void updateWithoutPasswordKeepsCurrentHash() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("novo@eai.com", user.getId())).thenReturn(false);
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, COMPANY_ID, TenantStatus.ACTIVE));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.updateUser(user.getId(), updateCommand("novo@eai.com", Set.of(UserRole.SELLER), COMPANY_ID, STORE_ID));

        assertThat(result.getPasswordHash()).isEqualTo("hash");
    }

    @DisplayName("Atribuicao de tenant atualiza empresa e loja")
    @Test
    void assignTenantUpdatesCompanyAndStore() {
        User user = user(UserRole.SELLER, OTHER_COMPANY_ID, OTHER_STORE_ID);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, COMPANY_ID, TenantStatus.ACTIVE));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.assignTenant(user.getId(), new AssignUserTenantCommand(COMPANY_ID, STORE_ID));

        assertThat(result.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(result.getStoreId()).isEqualTo(STORE_ID);
    }

    @DisplayName("Ativacao marca usuario ativo")
    @Test
    void activateUserMarksUserActive() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        user.deactivate();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.activateUser(user.getId());

        assertThat(result.isActive()).isTrue();
    }

    @DisplayName("Validacao de tenant rejeita empresa e loja inativas ou divergentes")
    @Test
    void tenantValidationRejectsInactiveOrMismatchedTenant() {
        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.INACTIVE));
        assertThatThrownBy(() -> service.createUser(command(UserRole.MANAGER, COMPANY_ID, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("company must be active");

        when(companyService.findRequired(COMPANY_ID)).thenReturn(company(TenantStatus.ACTIVE));
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, OTHER_COMPANY_ID, TenantStatus.ACTIVE));
        assertThatThrownBy(() -> service.createUser(command(UserRole.SELLER, COMPANY_ID, STORE_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("store does not belong");

        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, COMPANY_ID, TenantStatus.INACTIVE));
        assertThatThrownBy(() -> service.createUser(command(UserRole.SELLER, COMPANY_ID, STORE_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("company and store must be active");
    }

    @DisplayName("Papel admin nao pode ser vinculado a loja e usuario precisa ter papel unico")
    @Test
    void roleValidationRejectsAdminStoreAndMultipleRoles() {
        assertThatThrownBy(() -> service.createUser(command(UserRole.ADMIN, null, STORE_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN");

        assertThatThrownBy(() -> service.createUser(new CreateUserCommand("User", "user@eai.com", "secret123", null, null, COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER, UserRole.MANAGER))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly one role");
    }

    @DisplayName("Desativacao revoga refresh tokens")
    @Test
    void deactivateRevokesRefreshTokens() {
        User user = user(UserRole.SELLER, COMPANY_ID, STORE_ID);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deactivateUser(user.getId());

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    private CreateUserCommand command(UserRole role, UUID companyId, UUID storeId) {
        return new CreateUserCommand("User", role.name().toLowerCase() + "@eai.com", "secret123", null, null, companyId, storeId, Set.of(role));
    }

    private UpdateUserCommand updateCommand(String email, Set<UserRole> roles, UUID companyId, UUID storeId) {
        return new UpdateUserCommand("Novo User", email, null, "11999999999", "Cargo", companyId, storeId, roles);
    }

    private Company company(TenantStatus status) {
        return new Company(COMPANY_ID, "EAI", status, NOW, NOW);
    }

    private Store store(UUID id, UUID companyId, TenantStatus status) {
        return new Store(id, companyId, "Loja", "00000000000192", null, null, null, null, null, status, NOW, NOW);
    }

    private User user(UserRole role, UUID companyId, UUID storeId) {
        return new User(UUID.randomUUID(), "User", "user@eai.com", "hash", null, null, companyId, storeId, UserStatus.ACTIVE, Set.of(role), NOW, NOW);
    }

    private AuthenticatedUser authenticated(UserRole role, UUID companyId, UUID storeId) {
        return new AuthenticatedUser(UUID.randomUUID(), role.name().toLowerCase() + "@eai.com", companyId, storeId, Set.of(role));
    }
}
