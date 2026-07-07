package com.eai.api.email;

import com.eai.domain.email.EmailProtocol;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmailAccountCreateRequest(
        @NotNull UUID companyId,
        @NotNull UUID storeId,
        @NotBlank String name,
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @NotBlank String username,
        @NotBlank String password,
        EmailProtocol protocol,
        boolean useSsl,
        boolean active
) {
}
