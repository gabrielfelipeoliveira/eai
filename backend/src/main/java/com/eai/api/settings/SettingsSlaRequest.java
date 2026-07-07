package com.eai.api.settings;

import jakarta.validation.constraints.Min;

import java.util.UUID;

public record SettingsSlaRequest(
        UUID companyId,
        UUID storeId,
        @Min(1) int minutesToAssign,
        @Min(1) int minutesToFirstContact,
        boolean active
) {
}
