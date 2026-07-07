package com.eai.api.lead;

import com.eai.domain.lead.LeadStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeadStatusRequest(
        @NotNull LeadStatus status,
        @Size(max = 500) String description
) {
}
