package com.eai.application.email;

import com.eai.application.lead.LeadHistoryRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.PhoneNormalizer;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailImportHistory;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailLeadImporter {

    private final EmailAccountRepository emailAccountRepository;
    private final EmailReader emailReader;
    private final LeadExtractor leadExtractor;
    private final DuplicateLeadChecker duplicateLeadChecker;
    private final LeadRepository leadRepository;
    private final LeadHistoryRepository historyRepository;
    private final EmailImportHistoryRepository importHistoryRepository;
    private final EncryptionService encryptionService;

    public EmailLeadImporter(
            EmailAccountRepository emailAccountRepository,
            EmailReader emailReader,
            LeadExtractor leadExtractor,
            DuplicateLeadChecker duplicateLeadChecker,
            LeadRepository leadRepository,
            LeadHistoryRepository historyRepository,
            EmailImportHistoryRepository importHistoryRepository,
            EncryptionService encryptionService
    ) {
        this.emailAccountRepository = emailAccountRepository;
        this.emailReader = emailReader;
        this.leadExtractor = leadExtractor;
        this.duplicateLeadChecker = duplicateLeadChecker;
        this.leadRepository = leadRepository;
        this.historyRepository = historyRepository;
        this.importHistoryRepository = importHistoryRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public EmailImportResult importAccount(EmailAccount account, UUID userId) {
        Instant startedAt = Instant.now();
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
                Optional<Lead> duplicateLead = duplicateLeadChecker.findPossibleDuplicate(account.getStoreId(), parsedLead.customerPhone());
                boolean duplicate = duplicateLead.isPresent();
                Lead lead = createLead(account, parsedLead, duplicateLead.map(Lead::getId).orElse(null));
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
            importHistoryRepository.save(EmailImportHistory.success(account, messages.size(), created, duplicated, resultMessage, startedAt));
            return new EmailImportResult(messages.size(), created, duplicated, "SUCCESS", resultMessage);
        } catch (RuntimeException exception) {
            String failureMessage = exception.getMessage();
            account.recordFailure(failureMessage);
            emailAccountRepository.save(account);
            importHistoryRepository.save(EmailImportHistory.failure(account, failureMessage, startedAt));
            return new EmailImportResult(0, 0, 0, "FAILED", failureMessage);
        }
    }

    @Transactional
    public void importActiveAccounts() {
        emailAccountRepository.findActive().forEach(account -> importAccount(account, null));
    }

    private Lead createLead(EmailAccount account, ParsedEmailLead parsedLead, UUID relatedLeadId) {
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
                List.of(),
                parsedLead.customerEmail(),
                null,
                parsedLead.vehicleInterest(),
                null,
                null,
                LeadSource.EMAIL,
                parsedLead.originalMessage(),
                relatedLeadId == null ? LeadStatus.NEW : LeadStatus.DUPLICATED,
                null,
                null,
                now,
                now,
                null,
                null,
                null,
                null,
                null,
                relatedLeadId
        );
    }
}
