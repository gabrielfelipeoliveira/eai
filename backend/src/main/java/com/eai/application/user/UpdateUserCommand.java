package com.eai.application.user;

import com.eai.domain.user.UserRole;

import java.util.Set;
import java.util.UUID;

public record UpdateUserCommand(
        String name,
        String email,
        String password,
        String phone,
        String jobTitle,
        UUID companyId,
        UUID storeId,
        Set<UserRole> roles
) {
}
