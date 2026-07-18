package com.eai.application.email;

import com.eai.application.lead.LeadHistoryRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.PhoneNormalizer;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EmailLeadImporter {

    private final EmailAccountRepository emailAccountRepository;
    private final EmailReader emailReader;
    private final LeadExtractor leadExtractor;
    private final DuplicateLeadChecker duplicateLeadChecker;
    private final LeadRepository leadRepository;
    private final LeadHistoryRepository historyRepository;
    private final EncryptionService encryptionService;

    public EmailLeadImporter(
            EmailAccountRepository emailAccountRepository,
            EmailReader emailReader,
            LeadExtractor leadExtractor,
            DuplicateLeadChecker duplicateLeadChecker,
            LeadRepository leadRepository,
            LeadHistoryRepository historyRepository,
            EncryptionService encryptionService
    ) {
        this.emailAccountRepository = emailAccountRepository;
        this.emailReader = emailReader;
        this.leadExtractor = leadExtractor;
        this.duplicateLeadChecker = duplicateLeadChecker;
        this.leadRepository = leadRepository;
        this.historyRepository = historyRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public EmailImportResult importAccount(EmailAccount account, UUID userId) {
        try {
            var messages = emailReader.readMessages(account, encryptionService.decrypt(account.getEncryptedPassword()), account.getLastReadAt());
            int created = 0;
            int duplicated = 0;
            Instant newestReadAt = account.getLastReadAt();
            for (EmailMessage message : messages) {
                ParsedEmailLead parsedLead = leadExtractor.extract(message);
                if (parsedLead == null) {
                    continue;
                }
                boolean duplicate = duplicateLeadChecker.isPossibleDuplicate(account.getStoreId(), parsedLead.customerPhone(), parsedLead.vehicleInterest());
                Lead lead = createLead(account, parsedLead, duplicate);
                Lead savedLead = leadRepository.save(lead);
                historyRepository.save(LeadHistory.create(
                        savedLead.getId(),
                        userId,
                        null,
                        savedLead.getStatus(),
                        duplicate ? "Possivel duplicidade detectada na importacao por e-mail" : "Lead importado por e-mail"
                ));
                if (duplicate) {
                    duplicated++;
                } else {
                    created++;
                }
                if (message.receivedAt() != null && (newestReadAt == null || message.receivedAt().isAfter(newestReadAt))) {
                    newestReadAt = message.receivedAt();
                }
            }
            String resultMessage = "Mensagens lidas: " + messages.size() + ", leads criados: " + created + ", possiveis duplicados: " + duplicated;
            account.recordSuccess(newestReadAt == null ? Instant.now() : newestReadAt, resultMessage);
            emailAccountRepository.save(account);
            return new EmailImportResult(messages.size(), created, duplicated, "SUCCESS", resultMessage);
        } catch (RuntimeException exception) {
            account.recordFailure(exception.getMessage());
            emailAccountRepository.save(account);
            throw exception;
        }
    }

    @Transactional
    public void importActiveAccounts() {
        emailAccountRepository.findActive().forEach(account -> importAccount(account, null));
    }

    private Lead createLead(EmailAccount account, ParsedEmailLead parsedLead, boolean duplicate) {
        Instant now = Instant.now();
        String customerName = parsedLead.customerName() == null || parsedLead.customerName().isBlank()
                ? "Lead por e-mail"
                : parsedLead.customerName();
        return new Lead(
                UUID.randomUUID(),
                account.getCompanyId(),
                account.getStoreId(),
                customerName,
                PhoneNormalizer.normalize(parsedLead.customerPhone()),
                parsedLead.customerEmail(),
                null,
                parsedLead.vehicleInterest(),
                LeadSource.EMAIL,
                parsedLead.originalMessage(),
                duplicate ? LeadStatus.DUPLICATED : LeadStatus.NEW,
                null,
                null,
                now,
                now,
                null,
                null,
                null,
                null
        );
    }
}
