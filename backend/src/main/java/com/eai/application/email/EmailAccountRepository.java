package com.eai.application.email;

import com.eai.domain.email.EmailAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailAccountRepository {

    List<EmailAccount> findAll();

    List<EmailAccount> findActive();

    List<EmailAccount> findByCompanyId(UUID companyId);

    List<EmailAccount> findByStoreIdIn(List<UUID> storeIds);

    Optional<EmailAccount> findById(UUID id);

    EmailAccount save(EmailAccount account);

    void deleteById(UUID id);
}
