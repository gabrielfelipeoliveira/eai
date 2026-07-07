package com.eai.api.user;

import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String jobTitle,
        UserStatus status,
        Set<UserRole> roles,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getJobTitle(),
                user.getStatus(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
