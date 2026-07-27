package com.eai.api.user;

import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserService;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
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

class UserControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final AuthenticatedUser AUTHENTICATED_USER = new AuthenticatedUser(UUID.randomUUID(), "admin@eai.com", null, null, Set.of(UserRole.ADMIN));

    private final UserService userService = mock(UserService.class);
    private final UserController controller = new UserController(userService);

    @DisplayName("Lista e busca usuarios convertendo dominio para resposta")
    @Test
    void listAndGetUsersMapDomainToResponse() {
        when(userService.listUsers(AUTHENTICATED_USER)).thenReturn(List.of(user()));
        when(userService.getUser(USER_ID, AUTHENTICATED_USER)).thenReturn(user());

        assertThat(controller.listUsers(AUTHENTICATED_USER).getFirst().email()).isEqualTo("usuario@eai.com");
        assertThat(controller.getUser(USER_ID, AUTHENTICATED_USER).roles()).containsExactly(UserRole.SELLER);
    }

    @DisplayName("Cria e atualiza usuario repassando comandos ao servico")
    @Test
    void createAndUpdateUserDelegateCommands() {
        when(userService.createUser(any())).thenReturn(user());
        when(userService.updateUser(eq(USER_ID), any())).thenReturn(user());

        UserResponse created = controller.createUser(new UserCreateRequest(
                "Usuario", "usuario@eai.com", "secret1", "11999990000", "Vendedor", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER)
        ));
        UserResponse updated = controller.updateUser(USER_ID, new UserUpdateRequest(
                "Usuario", "usuario@eai.com", "", "11999990000", "Vendedor", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER)
        ));

        assertThat(created.id()).isEqualTo(USER_ID);
        assertThat(updated.companyId()).isEqualTo(COMPANY_ID);
        verify(userService).createUser(any());
        verify(userService).updateUser(eq(USER_ID), any());
    }

    @DisplayName("Atribui tenant, ativa e desativa usuario por id")
    @Test
    void assignTenantActivateAndDeactivateDelegateById() {
        when(userService.assignTenant(eq(USER_ID), any())).thenReturn(user());
        when(userService.activateUser(USER_ID)).thenReturn(user());
        when(userService.deactivateUser(USER_ID)).thenReturn(user());

        assertThat(controller.assignTenant(USER_ID, new UserTenantRequest(COMPANY_ID, STORE_ID)).storeId()).isEqualTo(STORE_ID);
        assertThat(controller.activateUser(USER_ID).status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(controller.deactivateUser(USER_ID).id()).isEqualTo(USER_ID);
    }

    private User user() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new User(
                USER_ID,
                "Usuario",
                "usuario@eai.com",
                "hash",
                "11999990000",
                "Vendedor",
                COMPANY_ID,
                STORE_ID,
                UserStatus.ACTIVE,
                Set.of(UserRole.SELLER),
                now,
                now
        );
    }
}
