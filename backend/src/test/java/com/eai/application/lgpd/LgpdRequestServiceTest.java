package com.eai.application.lgpd;

import com.eai.application.common.ForbiddenException;
import com.eai.application.lead.LeadRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.domain.lgpd.LgpdActionType;
import com.eai.domain.lgpd.LgpdRequest;
import com.eai.domain.lgpd.LgpdRequestStatus;
import com.eai.domain.lgpd.LgpdRequestType;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.TenantStatus;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LgpdRequestServiceTest {

    private final LgpdRequestRepository requestRepository = mock(LgpdRequestRepository.class);
    private final LgpdRequestActionRepository actionRepository = mock(LgpdRequestActionRepository.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final LgpdRequestService service = new LgpdRequestService(
            requestRepository,
            actionRepository,
            companyService,
            storeService,
            leadRepository
    );

    @DisplayName("Admin cria solicitacao LGPD valida")
    @Test
    void adminCreatesValidLgpdRequest() {
        UUID companyId = UUID.randomUUID();
        AuthenticatedUser admin = user(UserRole.ADMIN);
        when(companyService.findRequired(companyId)).thenReturn(new Company(
                companyId,
                "Empresa",
                TenantStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        ));
        when(requestRepository.save(any(LgpdRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LgpdRequest created = service.create(new CreateLgpdRequestCommand(
                companyId,
                null,
                null,
                "Titular",
                null,
                "titular@example.com",
                LgpdRequestType.ACCESS,
                "Solicitacao de acesso"
        ), admin);

        assertThat(created.getStatus()).isEqualTo(LgpdRequestStatus.OPEN);
        assertThat(created.getRequestedByUserId()).isEqualTo(admin.id());
    }

    @DisplayName("Bloqueia criacao de solicitacao LGPD sem ADMIN")
    @Test
    void blocksCreateWithoutAdmin() {
        assertThatThrownBy(() -> service.create(new CreateLgpdRequestCommand(
                UUID.randomUUID(),
                null,
                null,
                "Titular",
                null,
                null,
                LgpdRequestType.ACCESS,
                "Solicitacao"
        ), user(UserRole.MANAGER))).isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Registra acao manual com executor e data")
    @Test
    void registersManualActionWithExecutorAndDate() {
        UUID requestId = UUID.randomUUID();
        AuthenticatedUser admin = user(UserRole.ADMIN);
        LgpdRequest request = existingRequest(requestId, LgpdRequestStatus.OPEN);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(LgpdRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.findByRequestId(requestId)).thenAnswer(invocation -> List.of());

        service.registerAction(requestId, new RegisterLgpdRequestActionCommand(
                LgpdActionType.ACCESS,
                "Relatorio entregue manualmente",
                null
        ), admin);

        verify(actionRepository).save(org.mockito.ArgumentMatchers.argThat(action ->
                action.getExecutorUserId().equals(admin.id())
                        && action.getCreatedAt() != null
                        && action.getActionType() == LgpdActionType.ACCESS
        ));
        assertThat(request.getStatus()).isEqualTo(LgpdRequestStatus.IN_PROGRESS);
    }

    @DisplayName("Fecha solicitacao LGPD como concluida ou rejeitada")
    @Test
    void closesRequestAsCompletedOrRejected() {
        UUID completedId = UUID.randomUUID();
        LgpdRequest completed = existingRequest(completedId, LgpdRequestStatus.OPEN);
        when(requestRepository.findById(completedId)).thenReturn(Optional.of(completed));
        when(requestRepository.save(any(LgpdRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.findByRequestId(completedId)).thenReturn(List.of());

        service.registerAction(completedId, new RegisterLgpdRequestActionCommand(
                LgpdActionType.DELETION,
                "Solicitacao tratada manualmente",
                LgpdRequestStatus.COMPLETED
        ), user(UserRole.ADMIN));

        assertThat(completed.getStatus()).isEqualTo(LgpdRequestStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @DisplayName("Acao LGPD nao executa anonimização, eliminação ou alteração automatica de dados")
    @Test
    void actionDoesNotMutateLeadDataAutomatically() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(existingRequest(requestId, LgpdRequestStatus.OPEN)));
        when(requestRepository.save(any(LgpdRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.findByRequestId(requestId)).thenReturn(List.of());

        service.registerAction(requestId, new RegisterLgpdRequestActionCommand(
                LgpdActionType.ANONYMIZATION,
                "Anonimizacao registrada como procedimento manual",
                LgpdRequestStatus.COMPLETED
        ), user(UserRole.ADMIN));

        verify(leadRepository, never()).save(any());
    }

    private LgpdRequest existingRequest(UUID id, LgpdRequestStatus status) {
        return new LgpdRequest(
                id,
                UUID.randomUUID(),
                null,
                null,
                "Titular",
                null,
                null,
                LgpdRequestType.ACCESS,
                status,
                "Solicitacao",
                UUID.randomUUID(),
                Instant.now(),
                Instant.now(),
                null
        );
    }

    private AuthenticatedUser user(UserRole role) {
        return new AuthenticatedUser(UUID.randomUUID(), role.name().toLowerCase() + "@eai.com", UUID.randomUUID(), null, Set.of(role));
    }
}
