package com.eai.api.user;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserTenantRequest(@NotNull UUID companyId, @NotNull UUID storeId) {
}
