package com.eai.application.email;

public record EmailCredentialReencryptionResult(
        int evaluated,
        int migrated,
        int ignored,
        int failed
) {
}
