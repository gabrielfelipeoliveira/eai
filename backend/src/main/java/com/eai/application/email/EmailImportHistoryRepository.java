package com.eai.application.email;

import com.eai.domain.email.EmailImportHistory;

public interface EmailImportHistoryRepository {

    EmailImportHistory save(EmailImportHistory history);
}
