package com.eai.api.lead;

import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LeadRequest(
        @NotNull UUID companyId,
        @NotNull UUID storeId,
        @NotBlank @Size(max = 160) String customerName,
        @Size(max = 40) String customerPhone,
        @Email @Size(max = 180) String customerEmail,
        @Size(max = 120) String customerCity,
        @Size(max = 180) String vehicleInterest,
        @NotNull LeadSource source,
        String originalMessage,
        LeadStatus status,
        UUID assignedToUserId,
        Instant firstContactAt,
        Instant lastContactAt,
        @Size(max = 240) String lostReason,
        @DecimalMin("0.00") BigDecimal saleValue
) {
}
