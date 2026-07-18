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
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

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
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.name = requireText(name, "name");
        this.type = Objects.requireNonNull(type);
        this.content = requireText(content, "content");
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MessageTemplate create(UUID companyId, UUID storeId, String name, MessageTemplateType type, String content, boolean active) {
        Instant now = Instant.now();
        return new MessageTemplate(UUID.randomUUID(), companyId, storeId, name, type, content, active, now, now);
    }

    public void update(UUID companyId, UUID storeId, String name, MessageTemplateType type, String content, boolean active) {
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.name = requireText(name, "name");
        this.type = Objects.requireNonNull(type);
        this.content = requireText(content, "content");
        this.active = active;
        this.updatedAt = Instant.now();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
