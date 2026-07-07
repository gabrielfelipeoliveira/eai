package com.eai.domain.user;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String jobTitle;
    private UserStatus status;
    private final Set<UserRole> roles;
    private final Instant createdAt;
    private Instant updatedAt;

    public User(
            UUID id,
            String name,
            String email,
            String passwordHash,
            String phone,
            String jobTitle,
            UserStatus status,
            Set<UserRole> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "name");
        this.email = requireText(email, "email").toLowerCase();
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.phone = phone;
        this.jobTitle = jobTitle;
        this.status = Objects.requireNonNull(status);
        this.roles = roles.isEmpty() ? EnumSet.noneOf(UserRole.class) : EnumSet.copyOf(roles);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (this.roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
    }

    public static User create(
            String name,
            String email,
            String passwordHash,
            String phone,
            String jobTitle,
            Set<UserRole> roles
    ) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), name, email, passwordHash, phone, jobTitle, UserStatus.ACTIVE, roles, now, now);
    }

    public void updateProfile(String name, String email, String phone, String jobTitle, Set<UserRole> roles) {
        this.name = requireText(name, "name");
        this.email = requireText(email, "email").toLowerCase();
        this.phone = phone;
        this.jobTitle = jobTitle;
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        this.roles.clear();
        this.roles.addAll(roles);
        this.updatedAt = Instant.now();
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Set<UserRole> getRoles() {
        return Set.copyOf(roles);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
