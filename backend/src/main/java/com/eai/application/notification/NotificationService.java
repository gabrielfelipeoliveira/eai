package com.eai.application.notification;

import com.eai.application.common.NotFoundException;
import com.eai.application.email.EmailAccountFailureNotifier;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.notification.Notification;
import com.eai.domain.notification.NotificationSeverity;
import com.eai.domain.notification.NotificationType;
import com.eai.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService implements EmailAccountFailureNotifier {

    private static final int MAX_LIMIT = 100;
    private static final int MAX_ERROR_LENGTH = 500;

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryPort deliveryPort;
    private final UserRepository userRepository;

    @Transactional
    public Notification create(CreateNotificationCommand command) {
        Notification notification = Notification.create(
                command.recipientUserId(),
                command.type(),
                command.severity(),
                command.title(),
                command.message(),
                command.relatedEntityType(),
                command.relatedEntityId()
        );
        notification.updateExternalDeliveryStatus(deliveryPort.deliver(notification));
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<Notification> notifyActiveAdmins(
            NotificationType type,
            NotificationSeverity severity,
            String title,
            String message,
            String relatedEntityType,
            UUID relatedEntityId
    ) {
        return userRepository.findActiveByRole(UserRole.ADMIN).stream()
                .map(admin -> create(new CreateNotificationCommand(
                        admin.getId(),
                        type,
                        severity,
                        title,
                        message,
                        relatedEntityType,
                        relatedEntityId
                )))
                .toList();
    }

    @Override
    @Transactional
    public void notifyEmailAccountFailure(EmailAccount account, String operation, RuntimeException exception) {
        String error = sanitizeError(exception);
        String title = "Falha em conta de e-mail: " + account.getName();
        String message = """
                Operacao: %s
                Conta: %s
                Host: %s
                Usuario IMAP: %s
                companyId: %s
                storeId: %s
                Erro: %s
                """.formatted(
                sanitizeText(operation),
                account.getName(),
                account.getHost(),
                account.getUsername(),
                account.getCompanyId(),
                account.getStoreId(),
                error
        ).trim();
        notifyActiveAdmins(
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.ERROR,
                title,
                message,
                "EmailAccount",
                account.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<Notification> listMine(boolean unreadOnly, int limit, AuthenticatedUser authenticatedUser) {
        int normalizedLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        return notificationRepository.findByRecipient(authenticatedUser.id(), unreadOnly, normalizedLimit);
    }

    @Transactional(readOnly = true)
    public long countUnreadMine(AuthenticatedUser authenticatedUser) {
        return notificationRepository.countUnreadByRecipient(authenticatedUser.id());
    }

    @Transactional
    public Notification markRead(UUID id, AuthenticatedUser authenticatedUser) {
        Notification notification = notificationRepository.findByIdAndRecipientUserId(id, authenticatedUser.id())
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.markRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(AuthenticatedUser authenticatedUser) {
        notificationRepository.findUnreadByRecipient(authenticatedUser.id()).forEach(notification -> {
            notification.markRead();
            notificationRepository.save(notification);
        });
    }

    private static String sanitizeError(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        message = sanitizeText(message);
        if (message.length() > MAX_ERROR_LENGTH) {
            return message.substring(0, MAX_ERROR_LENGTH);
        }
        return message;
    }

    private static String sanitizeText(String value) {
        if (value == null || value.isBlank()) {
            return "Nao informado";
        }
        return value.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
