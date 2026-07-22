package com.eai.application.email;

import com.eai.domain.email.EmailAccount;

public interface EmailAccountFailureNotifier {

    void notifyEmailAccountFailure(EmailAccount account, String operation, RuntimeException exception);
}
