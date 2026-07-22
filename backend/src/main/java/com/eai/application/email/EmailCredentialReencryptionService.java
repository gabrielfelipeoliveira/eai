package com.eai.application.email;

import com.eai.domain.email.EmailAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailCredentialReencryptionService {

    private final EmailAccountRepository emailAccountRepository;
    private final EncryptionService encryptionService;

    @Transactional
    public EmailCredentialReencryptionResult reencryptAll() {
        int evaluated = 0;
        int migrated = 0;
        int ignored = 0;
        int failed = 0;

        for (EmailAccount account : emailAccountRepository.findAll()) {
            evaluated++;
            try {
                String encryptedPassword = account.getEncryptedPassword();
                if (!encryptionService.requiresReencryption(encryptedPassword)) {
                    ignored++;
                    continue;
                }
                String plainPassword = encryptionService.decrypt(encryptedPassword);
                account.replaceEncryptedPassword(encryptionService.encrypt(plainPassword));
                emailAccountRepository.save(account);
                migrated++;
            } catch (RuntimeException exception) {
                failed++;
            }
        }

        return new EmailCredentialReencryptionResult(evaluated, migrated, ignored, failed);
    }
}
