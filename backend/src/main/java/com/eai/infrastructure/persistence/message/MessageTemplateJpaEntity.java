package com.eai.infrastructure.persistence.message;

import lombok.Getter;
import lombok.Setter;

import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;
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
@Table(name = "message_templates")
public class MessageTemplateJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageTemplateType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "language_code", nullable = false)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "meta_status", nullable = false)
    private MessageTemplateMetaStatus metaStatus;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}
