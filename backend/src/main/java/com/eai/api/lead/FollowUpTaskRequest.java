package com.eai.api.lead;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record FollowUpTaskRequest(
        UUID userId,
        @NotBlank @Size(max = 160) String title,
        @Size(max = 1000) String description,
        @NotNull @FutureOrPresent Instant dueAt
) {
}
