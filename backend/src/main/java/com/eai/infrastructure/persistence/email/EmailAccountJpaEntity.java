package com.eai.infrastructure.persistence.email;

import lombok.Getter;
import lombok.Setter;

import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_accounts")
public class EmailAccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    private String username;

    @Column(name = "encrypted_password", nullable = false)
    private String encryptedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailProtocol protocol;

    @Column(name = "use_ssl", nullable = false)
    private boolean useSsl;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_sync_status", nullable = false)
    private EmailAccountStatus lastSyncStatus;

    @Column(name = "last_sync_message", columnDefinition = "TEXT")
    private String lastSyncMessage;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

}
