package com.eai.application.user;

import com.eai.domain.user.UserRole;

import java.util.Set;

public record CreateUserCommand(
        String name,
        String email,
        String password,
        String phone,
        String jobTitle,
        Set<UserRole> roles
) {
}
