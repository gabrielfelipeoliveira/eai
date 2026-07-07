package com.eai.api.lead;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeadTagRequest(@NotBlank @Size(max = 80) String name) {
}
