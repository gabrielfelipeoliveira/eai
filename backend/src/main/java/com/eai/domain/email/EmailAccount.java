package com.eai.domain.email;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class EmailAccount {

    private final UUID id;
    private UUID companyId;
    private UUID storeId;
    private String name;
    private String host;
    private int port;
    private String username;
    private String encryptedPassword;
    private EmailProtocol protocol;
    private boolean useSsl;
    private boolean active;
    private Instant lastReadAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private EmailAccountStatus lastSyncStatus;
    private String lastSyncMessage;
    private Instant lastSyncAt;

    public EmailAccount(
            UUID id,
            UUID companyId,
            UUID storeId,
            String name,
            String host,
            int port,
            String username,
            String encryptedPassword,
            EmailProtocol protocol,
            boolean useSsl,
            boolean active,
            Instant lastReadAt,
            Instant createdAt,
            Instant updatedAt,
            EmailAccountStatus lastSyncStatus,
            String lastSyncMessage,
            Instant lastSyncAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.name = requireText(name, "name");
        this.host = requireText(host, "host");
        this.port = requirePort(port);
        this.username = requireText(username, "username");
        this.encryptedPassword = requireText(encryptedPassword, "encryptedPassword");
        this.protocol = Objects.requireNonNull(protocol);
        this.useSsl = useSsl;
        this.active = active;
        this.lastReadAt = lastReadAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.lastSyncStatus = lastSyncStatus == null ? EmailAccountStatus.NEVER_SYNCED : lastSyncStatus;
        this.lastSyncMessage = trimToNull(lastSyncMessage);
        this.lastSyncAt = lastSyncAt;
    }

    public static EmailAccount create(
            UUID companyId,
            UUID storeId,
            String name,
            String host,
            int port,
            String username,
            String encryptedPassword,
            EmailProtocol protocol,
            boolean useSsl,
            boolean active
    ) {
        Instant now = Instant.now();
        return new EmailAccount(
                UUID.randomUUID(),
                companyId,
                storeId,
                name,
                host,
                port,
                username,
                encryptedPassword,
                protocol,
                useSsl,
                active,
                null,
                now,
                now,
                EmailAccountStatus.NEVER_SYNCED,
                null,
                null
        );
    }

    public void update(
            UUID companyId,
            UUID storeId,
            String name,
            String host,
            int port,
            String username,
            String encryptedPassword,
            EmailProtocol protocol,
            boolean useSsl,
            boolean active
    ) {
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.name = requireText(name, "name");
        this.host = requireText(host, "host");
        this.port = requirePort(port);
        this.username = requireText(username, "username");
        this.encryptedPassword = requireText(encryptedPassword, "encryptedPassword");
        this.protocol = Objects.requireNonNull(protocol);
        this.useSsl = useSsl;
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public void recordSuccess(Instant readAt, String message) {
        this.lastReadAt = readAt;
        this.lastSyncStatus = EmailAccountStatus.SUCCESS;
        this.lastSyncMessage = trimToNull(message);
        this.lastSyncAt = Instant.now();
        this.updatedAt = this.lastSyncAt;
    }

    public void recordFailure(String message) {
        this.lastSyncStatus = EmailAccountStatus.FAILED;
        this.lastSyncMessage = trimToNull(message);
        this.lastSyncAt = Instant.now();
        this.updatedAt = this.lastSyncAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static int requirePort(int value) {
        if (value < 1 || value > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        return value;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
