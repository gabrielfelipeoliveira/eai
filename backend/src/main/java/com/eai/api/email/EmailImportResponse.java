package com.eai.api.email;

import com.eai.application.email.EmailImportResult;

public record EmailImportResponse(
        int messagesRead,
        int leadsCreated,
        int duplicatesMarked,
        String status,
        String message
) {

    public static EmailImportResponse fromResult(EmailImportResult result) {
        return new EmailImportResponse(
                result.messagesRead(),
                result.leadsCreated(),
                result.duplicatesMarked(),
                result.status(),
                result.message()
        );
    }
}
