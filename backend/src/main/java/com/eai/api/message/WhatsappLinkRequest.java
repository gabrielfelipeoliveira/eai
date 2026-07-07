package com.eai.api.message;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WhatsappLinkRequest(@NotNull UUID templateId) {
}
