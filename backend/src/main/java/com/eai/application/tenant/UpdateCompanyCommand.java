package com.eai.application.tenant;

import com.eai.domain.tenant.TenantStatus;

public record UpdateCompanyCommand(String name, TenantStatus status) {
}
