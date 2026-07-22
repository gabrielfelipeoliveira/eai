package com.eai.infrastructure.email;

import com.eai.application.email.EmailCredentialReencryptionResult;
import com.eai.application.email.EmailCredentialReencryptionService;
import com.eai.infrastructure.config.EmailCredentialEncryptionProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailCredentialReencryptionJob implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EmailCredentialReencryptionJob.class);

    private final EmailCredentialEncryptionProperties properties;
    private final EmailCredentialReencryptionService emailCredentialReencryptionService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.reencryptOnStartupEnabled()) {
            return;
        }

        EmailCredentialReencryptionResult result = emailCredentialReencryptionService.reencryptAll();
        logger.info(
                "Recriptografia de credenciais IMAP concluida: avaliadas={}, migradas={}, ignoradas={}, falhas={}",
                result.evaluated(),
                result.migrated(),
                result.ignored(),
                result.failed()
        );
    }
}
