package com.eai.application.lead;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.lead.FollowUpTask;
import com.eai.domain.lead.FollowUpTaskStatus;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

class FollowUpTaskServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID OTHER_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID OTHER_SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000302");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID LEAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000601");
    private static final Instant NOW = Instant.parse("2026-07-26T10:00:00Z");
    private static final Instant DUE_AT = Instant.parse("2026-07-27T10:00:00Z");

    private final FollowUpTaskRepository taskRepository = mock(FollowUpTaskRepository.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final FollowUpTaskService service = new FollowUpTaskService(taskRepository, leadRepository, historyRepository, userRepository);

    @DisplayName("Cria follow-up para o vendedor responsavel quando comando nao informa usuario")
    @Test
    void createUsesAssignedSellerAsDefaultOwner() {
        Lead lead = lead(STORE_ID, SELLER_ID);
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(user(SELLER_ID, STORE_ID, UserRole.SELLER)));
        when(taskRepository.save(any(FollowUpTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FollowUpTask task = service.create(
                LEAD_ID,
                new CreateFollowUpTaskCommand(null, "Ligar cliente", "Confirmar interesse", DUE_AT),
                manager()
        );

        assertThat(task.getLeadId()).isEqualTo(LEAD_ID);
        assertThat(task.getUserId()).isEqualTo(SELLER_ID);
        assertThat(task.getTitle()).isEqualTo("Ligar cliente");
        assertThat(task.getDescription()).isEqualTo("Confirmar interesse");
        assertThat(task.getDueAt()).isEqualTo(DUE_AT);
        assertThat(task.getStatus()).isEqualTo(FollowUpTaskStatus.PENDING);
        verify(historyRepository).save(any(LeadHistory.class));
    }

    @DisplayName("Cria follow-up para o proprio usuario quando lead nao tem vendedor")
    @Test
    void createUsesAuthenticatedUserWhenLeadIsUnassigned() {
        Lead lead = lead(STORE_ID, null);
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(user(SELLER_ID, STORE_ID, UserRole.SELLER)));
        when(taskRepository.save(any(FollowUpTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FollowUpTask task = service.create(
                LEAD_ID,
                new CreateFollowUpTaskCommand(null, "Retornar", null, DUE_AT),
                seller()
        );

        assertThat(task.getUserId()).isEqualTo(SELLER_ID);
        assertThat(task.getDescription()).isNull();
    }

    @DisplayName("Vendedor nao cria follow-up para outro usuario")
    @Test
    void sellerCannotCreateTaskForAnotherUser() {
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, SELLER_ID)));

        assertThatThrownBy(() -> service.create(
                LEAD_ID,
                new CreateFollowUpTaskCommand(OTHER_SELLER_ID, "Retornar", null, DUE_AT),
                seller()
        )).isInstanceOf(ForbiddenException.class);

        verify(userRepository, never()).findById(OTHER_SELLER_ID);
        verify(taskRepository, never()).save(any(FollowUpTask.class));
    }

    @DisplayName("Nao cria follow-up quando usuario indicado pertence a outra loja")
    @Test
    void createRejectsUserFromAnotherStore() {
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, null)));
        when(userRepository.findById(OTHER_SELLER_ID)).thenReturn(Optional.of(user(OTHER_SELLER_ID, OTHER_STORE_ID, UserRole.SELLER)));

        assertThatThrownBy(() -> service.create(
                LEAD_ID,
                new CreateFollowUpTaskCommand(OTHER_SELLER_ID, "Retornar", null, DUE_AT),
                manager()
        )).isInstanceOf(ForbiddenException.class);

        verify(taskRepository, never()).save(any(FollowUpTask.class));
    }

    @DisplayName("Lista follow-ups visiveis do vendedor restritos ao proprio usuario")
    @Test
    void listSellerScopesToOwnUser() {
        List<FollowUpTask> expected = List.of(task(FollowUpTaskStatus.PENDING, SELLER_ID));
        when(taskRepository.findVisible(COMPANY_ID, STORE_ID, SELLER_ID)).thenReturn(expected);

        List<FollowUpTask> result = service.list(seller());

        assertThat(result).isSameAs(expected);
    }

    @DisplayName("Lista meus follow-ups sempre restringe pelo usuario autenticado")
    @Test
    void listMyScopesToAuthenticatedUser() {
        List<FollowUpTask> expected = List.of(task(FollowUpTaskStatus.PENDING, SELLER_ID));
        when(taskRepository.findVisible(COMPANY_ID, STORE_ID, SELLER_ID)).thenReturn(expected);

        List<FollowUpTask> result = service.listMy(seller());

        assertThat(result).isSameAs(expected);
    }

    @DisplayName("Admin lista follow-ups sem escopo de empresa loja ou usuario")
    @Test
    void adminListsWithoutTenantScope() {
        service.list(admin());

        verify(taskRepository).findVisible(null, null, null);
    }

    @DisplayName("Lista follow-ups do lead apos validar acesso")
    @Test
    void listByLeadReturnsTasksAfterAccessValidation() {
        Lead lead = lead(STORE_ID, SELLER_ID);
        List<FollowUpTask> expected = List.of(task(FollowUpTaskStatus.PENDING, SELLER_ID));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead));
        when(taskRepository.findByLeadId(LEAD_ID)).thenReturn(expected);

        List<FollowUpTask> result = service.listByLead(LEAD_ID, seller());

        assertThat(result).isSameAs(expected);
    }

    @DisplayName("Vendedor nao lista follow-ups de lead de outra loja")
    @Test
    void sellerCannotListTasksFromOtherStoreLead() {
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(OTHER_STORE_ID, OTHER_SELLER_ID)));

        assertThatThrownBy(() -> service.listByLead(LEAD_ID, seller()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Conclui follow-up e registra historico")
    @Test
    void completeTaskSavesDoneStatusAndHistory() {
        FollowUpTask task = task(FollowUpTaskStatus.PENDING, SELLER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, SELLER_ID)));
        when(taskRepository.save(task)).thenReturn(task);
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FollowUpTask result = service.complete(TASK_ID, seller());

        assertThat(result.getStatus()).isEqualTo(FollowUpTaskStatus.DONE);
        assertThat(result.getCompletedAt()).isNotNull();
        verify(historyRepository).save(any(LeadHistory.class));
    }

    @DisplayName("Nao conclui follow-up cancelado")
    @Test
    void completeCanceledTaskRaisesConflict() {
        FollowUpTask task = task(FollowUpTaskStatus.CANCELED, SELLER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, SELLER_ID)));

        assertThatThrownBy(() -> service.complete(TASK_ID, seller()))
                .isInstanceOf(ConflictException.class);

        verify(taskRepository, never()).save(any(FollowUpTask.class));
    }

    @DisplayName("Cancela follow-up pendente e registra historico")
    @Test
    void cancelPendingTaskSavesCanceledStatusAndHistory() {
        FollowUpTask task = task(FollowUpTaskStatus.PENDING, SELLER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, SELLER_ID)));
        when(taskRepository.save(task)).thenReturn(task);

        FollowUpTask result = service.cancel(TASK_ID, seller());

        assertThat(result.getStatus()).isEqualTo(FollowUpTaskStatus.CANCELED);
        verify(historyRepository).save(any(LeadHistory.class));
    }

    @DisplayName("Nao cancela follow-up concluido")
    @Test
    void cancelDoneTaskRaisesConflict() {
        FollowUpTask task = task(FollowUpTaskStatus.DONE, SELLER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, SELLER_ID)));

        assertThatThrownBy(() -> service.cancel(TASK_ID, seller()))
                .isInstanceOf(ConflictException.class);

        verify(taskRepository, never()).save(any(FollowUpTask.class));
    }

    @DisplayName("Vendedor nao conclui follow-up de outro usuario")
    @Test
    void sellerCannotCompleteAnotherUserTask() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task(FollowUpTaskStatus.PENDING, OTHER_SELLER_ID)));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, OTHER_SELLER_ID)));

        assertThatThrownBy(() -> service.complete(TASK_ID, seller()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Falha quando follow-up nao existe")
    @Test
    void completeMissingTaskRaisesNotFound() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.complete(TASK_ID, seller()))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("Historico de conclusao preserva lead usuario e status")
    @Test
    void completeHistoryKeepsLeadUserAndStatus() {
        FollowUpTask task = task(FollowUpTaskStatus.PENDING, SELLER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(leadRepository.findById(LEAD_ID)).thenReturn(Optional.of(lead(STORE_ID, SELLER_ID)));
        when(taskRepository.save(task)).thenReturn(task);

        service.complete(TASK_ID, seller());

        ArgumentCaptor<LeadHistory> captor = ArgumentCaptor.forClass(LeadHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getLeadId()).isEqualTo(LEAD_ID);
        assertThat(captor.getValue().getUserId()).isEqualTo(SELLER_ID);
        assertThat(captor.getValue().getPreviousStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(captor.getValue().getNewStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(captor.getValue().getDescription()).contains("Follow-up completed");
    }

    private FollowUpTask task(FollowUpTaskStatus status, UUID userId) {
        return new FollowUpTask(
                TASK_ID,
                LEAD_ID,
                userId,
                "Ligar cliente",
                "Confirmar interesse",
                DUE_AT,
                status == FollowUpTaskStatus.DONE ? NOW : null,
                status,
                NOW,
                NOW
        );
    }

    private Lead lead(UUID storeId, UUID assignedToUserId) {
        return new Lead(
                LEAD_ID,
                COMPANY_ID,
                storeId,
                "Cliente Teste",
                "+5511999990000",
                "cliente@eai.com",
                "Sao Paulo",
                "Honda Civic",
                LeadSource.MANUAL,
                "Lead manual",
                assignedToUserId == null ? LeadStatus.AVAILABLE : LeadStatus.ASSIGNED,
                assignedToUserId,
                assignedToUserId == null ? null : NOW,
                NOW,
                NOW,
                null,
                null,
                null,
                null
        );
    }

    private User user(UUID id, UUID storeId, UserRole role) {
        return new User(
                id,
                "Usuario",
                "usuario-" + id + "@eai.com",
                "hash",
                null,
                null,
                COMPANY_ID,
                storeId,
                UserStatus.ACTIVE,
                Set.of(role),
                NOW,
                NOW
        );
    }

    private AuthenticatedUser seller() {
        return new AuthenticatedUser(SELLER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(MANAGER_ID, "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(MANAGER_ID, "admin@eai.com", null, null, Set.of(UserRole.ADMIN));
    }
}
