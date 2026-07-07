package com.eai.application.email;

import java.time.Instant;

public record EmailMessage(
        String subject,
        String from,
        String body,
        Instant receivedAt
) {
}
