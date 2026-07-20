package com.eai.infrastructure.persistence.email;

import com.eai.domain.email.EmailAccountStatus;
import lombok.Getter;
import lombok.Setter;

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
@Table(name = "email_import_history")
public class EmailImportHistoryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "email_account_id")
    private UUID emailAccountId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailAccountStatus status;

    @Column(name = "messages_read", nullable = false)
    private int messagesRead;

    @Column(name = "leads_created", nullable = false)
    private int leadsCreated;

    @Column(name = "duplicates_marked", nullable = false)
    private int duplicatesMarked;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;
}
