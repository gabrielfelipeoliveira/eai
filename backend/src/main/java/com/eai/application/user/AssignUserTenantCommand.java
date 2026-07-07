package com.eai.application.user;

import java.util.UUID;

public record AssignUserTenantCommand(UUID companyId, UUID storeId) {
}
