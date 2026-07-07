package com.eai.api.email;

import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;

import java.time.Instant;
import java.util.UUID;

public record EmailAccountResponse(
        UUID id,
        UUID companyId,
        UUID storeId,
        String name,
        String host,
        int port,
        String username,
        EmailProtocol protocol,
        boolean useSsl,
        boolean active,
        Instant lastReadAt,
        Instant createdAt,
        Instant updatedAt,
        EmailAccountStatus lastSyncStatus,
        String lastSyncMessage,
        Instant lastSyncAt
) {

    public static EmailAccountResponse fromDomain(EmailAccount account) {
        return new EmailAccountResponse(
                account.getId(),
                account.getCompanyId(),
                account.getStoreId(),
                account.getName(),
                account.getHost(),
                account.getPort(),
                account.getUsername(),
                account.getProtocol(),
                account.isUseSsl(),
                account.isActive(),
                account.getLastReadAt(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                account.getLastSyncStatus(),
                account.getLastSyncMessage(),
                account.getLastSyncAt()
        );
    }
}
