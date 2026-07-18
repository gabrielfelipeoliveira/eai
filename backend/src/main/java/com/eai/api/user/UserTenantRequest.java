package com.eai.api.user;

import java.util.UUID;

public record UserTenantRequest(UUID companyId, UUID storeId) {
}
