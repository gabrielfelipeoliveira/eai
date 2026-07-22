package com.eai.api.lead;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record LeadTagRequest(UUID tagId, @Size(max = 80) String name) {
}
