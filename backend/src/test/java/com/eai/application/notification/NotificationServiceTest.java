package com.eai.application.notification;

import com.eai.application.user.UserRepository;
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
