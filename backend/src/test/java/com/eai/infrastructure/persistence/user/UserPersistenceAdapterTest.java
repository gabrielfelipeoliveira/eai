package com.eai.infrastructure.persistence.user;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();

    private final SpringDataUserRepository repository = mock(SpringDataUserRepository.class);
    private final UserPersistenceAdapter adapter = new UserPersistenceAdapter(repository);

    @DisplayName("Consulta usuarios convertendo entidades JPA para dominio")
    @Test
    void findsUsersMappingEntitiesToDomain() {
        UserJpaEntity entity = entity();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(entity));
        when(repository.findByStoreId(STORE_ID)).thenReturn(List.of(entity));
        when(repository.findByStoreIdAndStatus(STORE_ID, UserStatus.ACTIVE)).thenReturn(List.of(entity));
        when(repository.findByStatusAndRole(UserStatus.ACTIVE, UserRole.SELLER)).thenReturn(List.of(entity));
        when(repository.findById(USER_ID)).thenReturn(Optional.of(entity));
        when(repository.findByEmail("usuario@eai.com")).thenReturn(Optional.of(entity));

        assertThat(adapter.findAll()).hasSize(1);
        assertThat(adapter.findByCompanyId(COMPANY_ID).getFirst().getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(adapter.findByStoreId(STORE_ID).getFirst().getStoreId()).isEqualTo(STORE_ID);
        assertThat(adapter.findActiveByStoreId(STORE_ID).getFirst().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(adapter.findActiveByRole(UserRole.SELLER).getFirst().getRoles()).contains(UserRole.SELLER);
        assertThat(adapter.findById(USER_ID)).isPresent();
        assertThat(adapter.findByEmail("usuario@eai.com")).isPresent();
    }

    @DisplayName("Verificacoes de existencia e salvamento delegam para repositorio")
    @Test
    void existenceAndSaveDelegateToRepository() {
        when(repository.existsByCompanyIdAndStatus(COMPANY_ID, UserStatus.ACTIVE)).thenReturn(true);
        when(repository.existsByEmail("usuario@eai.com")).thenReturn(true);
        when(repository.existsByEmailAndIdNot("usuario@eai.com", USER_ID)).thenReturn(true);
        when(repository.save(any(UserJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(adapter.existsActiveByCompanyId(COMPANY_ID)).isTrue();
        assertThat(adapter.existsByEmail("usuario@eai.com")).isTrue();
        assertThat(adapter.existsByEmailAndIdNot("usuario@eai.com", USER_ID)).isTrue();
        assertThat(adapter.save(user()).getId()).isEqualTo(USER_ID);
        verify(repository).save(any(UserJpaEntity.class));
    }

    private User user() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new User(USER_ID, "Usuario", "usuario@eai.com", "hash", "11999990000", "Vendedor", COMPANY_ID, STORE_ID, UserStatus.ACTIVE, Set.of(UserRole.SELLER), now, now);
    }

    private UserJpaEntity entity() {
        User user = user();
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPhone(user.getPhone());
        entity.setJobTitle(user.getJobTitle());
        entity.setCompanyId(user.getCompanyId());
        entity.setStoreId(user.getStoreId());
        entity.setStatus(user.getStatus());
        entity.setRoles(user.getRoles());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
