package com.eai.application.lead;

import java.time.Instant;
import java.util.UUID;

public record CreateFollowUpTaskCommand(UUID userId, String title, String description, Instant dueAt) {
}
