package com.eai.application.email;

import com.eai.domain.email.EmailProtocol;

import java.util.UUID;

public record UpdateEmailAccountCommand(
        UUID companyId,
        UUID storeId,
        String name,
        String host,
        int port,
        String username,
        String password,
        EmailProtocol protocol,
        boolean useSsl,
        boolean active
) {
}
