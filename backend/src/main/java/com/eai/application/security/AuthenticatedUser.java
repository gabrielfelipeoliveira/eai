package com.eai.application.security;

import com.eai.domain.user.UserRole;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, Set<UserRole> roles) {
}
