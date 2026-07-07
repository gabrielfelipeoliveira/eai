package com.eai.application.email;

public record EmailImportResult(
        int messagesRead,
        int leadsCreated,
        int duplicatesMarked,
        String status,
        String message
) {
}
