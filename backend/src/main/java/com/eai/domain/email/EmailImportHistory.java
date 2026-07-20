package com.eai.domain.email;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class EmailImportHistory {

    private final UUID id;
    private final UUID emailAccountId;
    private final UUID companyId;
    private final UUID storeId;
    private final EmailAccountStatus status;
    private final int messagesRead;
    private final int leadsCreated;
    private final int duplicatesMarked;
    private final String message;
    private final Instant startedAt;
    private final Instant finishedAt;

    public EmailImportHistory(
            UUID id,
            UUID emailAccountId,
            UUID companyId,
            UUID storeId,
            EmailAccountStatus status,
            int messagesRead,
            int leadsCreated,
            int duplicatesMarked,
            String message,
            Instant startedAt,
            Instant finishedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.emailAccountId = emailAccountId;
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.status = Objects.requireNonNull(status);
        this.messagesRead = requireNonNegative(messagesRead, "messagesRead");
        this.leadsCreated = requireNonNegative(leadsCreated, "leadsCreated");
        this.duplicatesMarked = requireNonNegative(duplicatesMarked, "duplicatesMarked");
        this.message = message == null || message.isBlank() ? null : message.trim();
        this.startedAt = Objects.requireNonNull(startedAt);
        this.finishedAt = Objects.requireNonNull(finishedAt);
    }

    public static EmailImportHistory success(EmailAccount account, int messagesRead, int leadsCreated, int duplicatesMarked, String message, Instant startedAt) {
        return new EmailImportHistory(UUID.randomUUID(), account.getId(), account.getCompanyId(), account.getStoreId(), EmailAccountStatus.SUCCESS, messagesRead, leadsCreated, duplicatesMarked, message, startedAt, Instant.now());
    }

    public static EmailImportHistory failure(EmailAccount account, String message, Instant startedAt) {
        return new EmailImportHistory(UUID.randomUUID(), account.getId(), account.getCompanyId(), account.getStoreId(), EmailAccountStatus.FAILED, 0, 0, 0, message, startedAt, Instant.now());
    }

    private int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
        return value;
    }
}
