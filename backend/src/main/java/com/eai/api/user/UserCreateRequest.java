package com.eai.api.user;

import com.eai.domain.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserCreateRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 6, max = 80) String password,
        @Size(max = 40) String phone,
        @Size(max = 120) String jobTitle,
        @NotEmpty Set<UserRole> roles
) {
}
