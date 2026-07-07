package com.eai.application.tenant;

import com.eai.domain.tenant.TenantStatus;

public record UpdateCompanyCommand(String name, String document, String email, String phone, TenantStatus status) {
}
