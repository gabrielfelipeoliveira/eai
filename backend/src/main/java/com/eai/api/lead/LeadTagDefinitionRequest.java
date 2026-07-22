package com.eai.api.lead;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeadTagDefinitionRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 40) String type
) {
}
