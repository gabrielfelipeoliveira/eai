package com.eai.application.email;

import com.eai.domain.email.EmailAccount;

import java.time.Instant;
import java.util.List;

public interface EmailReader {

    void testConnection(EmailAccount account, String password);

    List<EmailMessage> readMessages(EmailAccount account, String password, Instant since);
}
