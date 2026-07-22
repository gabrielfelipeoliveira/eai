package com.eai.domain.message;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MessageTemplate {

    private final UUID id;
    private UUID companyId;
    private UUID storeId;
    private String name;
    private MessageTemplateType type;
    private String content;
    private String languageCode;
    private MessageTemplateMetaStatus metaStatus;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public MessageTemplate(
            UUID id,
            UUID companyId,
            UUID storeId,
            String name,
            MessageTemplateType type,
            String content,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, companyId, storeId, name, type, content, null, MessageTemplateMetaStatus.APPROVED, active, createdAt, updatedAt, null);
    }

    public MessageTemplate(
            UUID id,
            UUID companyId,
            UUID storeId,
            String name,
            MessageTemplateType type,
            String content,
            String languageCode,
            MessageTemplateMetaStatus metaStatus,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = storeId;
        this.name = requireMetaName(name);
        this.type = Objects.requireNonNull(type);
        this.content = requireText(content, "content");
        this.languageCode = normalizeLanguageCode(languageCode);
        this.metaStatus = metaStatus == null ? MessageTemplateMetaStatus.PENDING : metaStatus;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.deletedAt = deletedAt;
    }

    public static MessageTemplate create(
            UUID companyId,
            UUID storeId,
            String name,
            MessageTemplateType type,
            String content,
            boolean active
    ) {
        return create(companyId, storeId, name, type, content, null, MessageTemplateMetaStatus.APPROVED, active);
    }

    public static MessageTemplate create(
            UUID companyId,
            UUID storeId,
            String name,
            MessageTemplateType type,
            String content,
            String languageCode,
            MessageTemplateMetaStatus metaStatus,
            boolean active
    ) {
        Instant now = Instant.now();
        return new MessageTemplate(UUID.randomUUID(), companyId, storeId, name, type, content, languageCode, metaStatus, active, now, now, null);
    }

    public void update(
            UUID companyId,
            UUID storeId,
            String name,
            MessageTemplateType type,
            String content,
            boolean active
    ) {
        update(companyId, storeId, name, type, content, languageCode, metaStatus, active);
    }

    public void update(
            UUID companyId,
            UUID storeId,
            String name,
            MessageTemplateType type,
            String content,
            String languageCode,
            MessageTemplateMetaStatus metaStatus,
            boolean active
    ) {
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = storeId;
        this.name = requireMetaName(name);
        this.type = Objects.requireNonNull(type);
        this.content = requireText(content, "content");
        this.languageCode = normalizeLanguageCode(languageCode);
        this.metaStatus = metaStatus == null ? MessageTemplateMetaStatus.PENDING : metaStatus;
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.active = false;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " required");
        }
        return value.trim();
    }

    private static String requireMetaName(String value) {
        String name = requireText(value, "name");
        if (!name.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("name must match the approved Meta template name using lowercase letters, numbers and underscores");
        }
        return name;
    }

    private static String normalizeLanguageCode(String value) {
        if (value == null || value.isBlank()) {
            return "pt-BR";
        }
        return value.trim();
    }
}
