package com.eai.application.tenant;

import java.util.UUID;

public record CreateStoreCommand(
        UUID companyId,
        String name,
        String document,
        String email,
        String phone,
        String city,
        String state,
        String address
) {
}
