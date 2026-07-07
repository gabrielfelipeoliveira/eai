package com.eai.infrastructure.email;

import com.eai.application.email.EmailLeadImporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "eai.email.importer", name = "enabled", havingValue = "true")
public class EmailImporterScheduler {

    private final EmailLeadImporter emailLeadImporter;

    public EmailImporterScheduler(EmailLeadImporter emailLeadImporter) {
        this.emailLeadImporter = emailLeadImporter;
    }

    @Scheduled(fixedDelayString = "${eai.email.importer.fixed-delay:60000}")
    public void importActiveAccounts() {
        emailLeadImporter.importActiveAccounts();
    }
}
