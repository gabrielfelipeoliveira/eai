package com.eai.application.notification;

import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.notification.Notification;
import com.eai.domain.notification.NotificationExternalDeliveryStatus;
import com.eai.domain.notification.NotificationSeverity;
import com.eai.domain.notification.NotificationType;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationDeliveryPort deliveryPort = mock(NotificationDeliveryPort.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final NotificationService service = new NotificationService(notificationRepository, deliveryPort, userRepository);

    @DisplayName("Cria notificacoes para todos os administradores ativos")
    @Test
    void createsNotificationForEveryActiveAdmin() {
        when(userRepository.findActiveByRole(UserRole.ADMIN)).thenReturn(List.of(
                user(ADMIN_ID, UserRole.ADMIN, UserStatus.ACTIVE),
                user(SECOND_ADMIN_ID, UserRole.ADMIN, UserStatus.ACTIVE)
        ));
        when(deliveryPort.deliver(any(Notification.class))).thenReturn(NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyActiveAdmins(
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.ERROR,
                "Falha em e-mail",
                "Conta indisponivel",
                "EmailAccount",
                UUID.randomUUID()
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notification::getRecipientUserId)
                .containsExactly(ADMIN_ID, SECOND_ADMIN_ID);
    }

    @DisplayName("Usuarios inativos e nao administradores nao recebem alerta quando a porta retorna apenas admins ativos")
    @Test
    void doesNotNotifyUsersOutsideActiveAdminQuery() {
        when(userRepository.findActiveByRole(UserRole.ADMIN)).thenReturn(List.of(user(ADMIN_ID, UserRole.ADMIN, UserStatus.ACTIVE)));
        when(deliveryPort.deliver(any(Notification.class))).thenReturn(NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyActiveAdmins(
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.ERROR,
                "Falha em e-mail",
                "Conta indisponivel",
                "EmailAccount",
                UUID.randomUUID()
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipientUserId()).isEqualTo(ADMIN_ID);
    }

    @DisplayName("Cria notificacao com status retornado pela porta de entrega")
    @Test
    void createStoresDeliveryStatusReturnedByPort() {
        when(deliveryPort.deliver(any(Notification.class))).thenReturn(NotificationExternalDeliveryStatus.DELIVERED);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = service.create(new CreateNotificationCommand(
                ADMIN_ID,
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.WARNING,
                "Titulo",
                "Mensagem",
                "EmailAccount",
                UUID.randomUUID()
        ));

        assertThat(notification.getExternalDeliveryStatus()).isEqualTo(NotificationExternalDeliveryStatus.DELIVERED);
    }

    @DisplayName("Listagem das minhas notificacoes limita o tamanho entre um e cem")
    @Test
    void listMineNormalizesLimitBetweenOneAndOneHundred() {
        AuthenticatedUser user = authenticatedUser();
        when(notificationRepository.findByRecipient(ADMIN_ID, true, 1)).thenReturn(List.of(notification()));
        when(notificationRepository.findByRecipient(ADMIN_ID, false, 100)).thenReturn(List.of(notification(), notification()));

        assertThat(service.listMine(true, 0, user)).hasSize(1);
        assertThat(service.listMine(false, 200, user)).hasSize(2);
    }

    @DisplayName("Conta notificacoes nao lidas do usuario autenticado")
    @Test
    void countUnreadMineUsesAuthenticatedUser() {
        when(notificationRepository.countUnreadByRecipient(ADMIN_ID)).thenReturn(7L);

        assertThat(service.countUnreadMine(authenticatedUser())).isEqualTo(7L);
    }

    @DisplayName("Marca notificacao como lida quando pertence ao usuario")
    @Test
    void markReadStoresReadNotification() {
        Notification notification = notification();
        when(notificationRepository.findByIdAndRecipientUserId(notification.getId(), ADMIN_ID)).thenReturn(java.util.Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification read = service.markRead(notification.getId(), authenticatedUser());

        assertThat(read.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @DisplayName("Marca leitura rejeita notificacao inexistente para o usuario")
    @Test
    void markReadRejectsUnknownNotification() {
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndRecipientUserId(notificationId, ADMIN_ID)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.markRead(notificationId, authenticatedUser()))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("Marca todas as notificacoes nao lidas do usuario")
    @Test
    void markAllReadStoresEveryUnreadNotification() {
        Notification first = notification();
        Notification second = notification();
        when(notificationRepository.findUnreadByRecipient(ADMIN_ID)).thenReturn(List.of(first, second));

        service.markAllRead(authenticatedUser());

        assertThat(first.isRead()).isTrue();
        assertThat(second.isRead()).isTrue();
        verify(notificationRepository).save(first);
        verify(notificationRepository).save(second);
    }

    @DisplayName("Falha de conta de email sanitiza operacao e limita erro notificado")
    @Test
    void notifyEmailAccountFailureSanitizesMessage() {
        when(userRepository.findActiveByRole(UserRole.ADMIN)).thenReturn(List.of(user(ADMIN_ID, UserRole.ADMIN, UserStatus.ACTIVE)));
        when(deliveryPort.deliver(any(Notification.class))).thenReturn(NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyEmailAccountFailure(
                emailAccount(),
                " leitura\timap\n",
                new RuntimeException("erro\n".repeat(200))
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).contains("Conta principal");
        assertThat(captor.getValue().getMessage())
                .contains("Operacao: leitura imap")
                .doesNotContain("\t");
        assertThat(captor.getValue().getMessage()).contains("Erro: " + "erro ".repeat(100).trim());
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(ADMIN_ID, "admin@eai.com", null, null, Set.of(UserRole.ADMIN));
    }

    private Notification notification() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Notification(
                UUID.randomUUID(),
                ADMIN_ID,
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.ERROR,
                "Titulo",
                "Mensagem",
                "EmailAccount",
                UUID.randomUUID(),
                NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY,
                null,
                now,
                now
        );
    }

    private EmailAccount emailAccount() {
        return EmailAccount.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Conta principal",
                "imap.eai.com",
                993,
                "contato@eai.com",
                "encrypted",
                EmailProtocol.IMAP,
                true,
                true
        );
    }

    private User user(UUID id, UserRole role, UserStatus status) {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new User(
                id,
                "Usuario",
                id + "@eai.com",
                "hash",
                null,
                null,
                null,
                null,
                status,
                Set.of(role),
                now,
                now
        );
    }
}
