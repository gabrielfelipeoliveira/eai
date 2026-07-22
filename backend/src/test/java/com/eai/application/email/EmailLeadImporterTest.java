package com.eai.application.email;

import com.eai.application.lead.LeadHistoryRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailImportHistory;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailLeadImporterTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID DUPLICATE_LEAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");

    private final EmailAccountRepository emailAccountRepository = mock(EmailAccountRepository.class);
    private final EmailReader emailReader = mock(EmailReader.class);
    private final LeadExtractor leadExtractor = mock(LeadExtractor.class);
    private final DuplicateLeadChecker duplicateLeadChecker = mock(DuplicateLeadChecker.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
    private final EmailImportHistoryRepository importHistoryRepository = mock(EmailImportHistoryRepository.class);
    private final EncryptionService encryptionService = mock(EncryptionService.class);
    private final EmailAccountFailureNotifier emailAccountFailureNotifier = mock(EmailAccountFailureNotifier.class);
    private final EmailLeadImporter importer = new EmailLeadImporter(
            emailAccountRepository,
            emailReader,
            leadExtractor,
            duplicateLeadChecker,
            leadRepository,
            historyRepository,
            importHistoryRepository,
            encryptionService,
            emailAccountFailureNotifier
    );

    @DisplayName("Importacao cria leads, marca duplicados e registra historicos")
    @Test
    void importsMessagesCreatesDuplicatesAndRecordsHistories() {
        EmailAccount account = account(null);
        EmailMessage firstMessage = message("Primeiro lead", "2026-07-07T12:00:00Z");
        EmailMessage duplicateMessage = message("Lead repetido", "2026-07-07T12:05:00Z");
        ParsedEmailLead firstParsed = parsed("Maria Souza", "(11) 99999-8888", "maria@example.com", "Honda Civic", "Mensagem 1");
        ParsedEmailLead duplicateParsed = parsed("Maria Souza", "(11) 99999-8888", "maria.novo@example.com", "Corolla", "Mensagem 2");
        Lead previousLead = lead(DUPLICATE_LEAD_ID, LeadStatus.ASSIGNED);

        when(encryptionService.decrypt("encrypted-password")).thenReturn("plain-password");
        when(emailReader.readMessages(account, "plain-password", null)).thenReturn(List.of(firstMessage, duplicateMessage));
        when(leadExtractor.extract(firstMessage)).thenReturn(firstParsed);
        when(leadExtractor.extract(duplicateMessage)).thenReturn(duplicateParsed);
        when(duplicateLeadChecker.findPossibleDuplicate(STORE_ID, "(11) 99999-8888"))
                .thenReturn(Optional.empty(), Optional.of(previousLead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(importHistoryRepository.save(any(EmailImportHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailImportResult result = importer.importAccount(account, USER_ID);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.messagesRead()).isEqualTo(2);
        assertThat(result.leadsCreated()).isEqualTo(1);
        assertThat(result.duplicatesMarked()).isEqualTo(1);
        assertThat(account.getLastReadAt()).isEqualTo(Instant.parse("2026-07-07T12:05:00Z"));
        assertThat(account.getLastSyncStatus()).isEqualTo(EmailAccountStatus.SUCCESS);

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepository, times(2)).save(leadCaptor.capture());
        assertThat(leadCaptor.getAllValues())
                .extracting(Lead::getStatus)
                .containsExactly(LeadStatus.NEW, LeadStatus.DUPLICATED);
        assertThat(leadCaptor.getAllValues().get(1).getRelatedLeadId()).isEqualTo(DUPLICATE_LEAD_ID);

        ArgumentCaptor<LeadHistory> historyCaptor = ArgumentCaptor.forClass(LeadHistory.class);
        verify(historyRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(LeadHistory::getNewStatus)
                .containsExactly(LeadStatus.NEW, LeadStatus.DUPLICATED);
        verify(emailReader).markMessagesAsRead(account, "plain-password", null, Instant.parse("2026-07-07T12:05:00Z"));

        ArgumentCaptor<EmailImportHistory> importHistoryCaptor = ArgumentCaptor.forClass(EmailImportHistory.class);
        verify(importHistoryRepository).save(importHistoryCaptor.capture());
        EmailImportHistory importHistory = importHistoryCaptor.getValue();
        assertThat(importHistory.getStatus()).isEqualTo(EmailAccountStatus.SUCCESS);
        assertThat(importHistory.getMessagesRead()).isEqualTo(2);
        assertThat(importHistory.getLeadsCreated()).isEqualTo(1);
        assertThat(importHistory.getDuplicatesMarked()).isEqualTo(1);
    }

    @DisplayName("Falha de importacao atualiza conta e registra historico")
    @Test
    void recordsFailureWhenReaderFails() {
        EmailAccount account = account(Instant.parse("2026-07-07T11:00:00Z"));

        when(encryptionService.decrypt("encrypted-password")).thenReturn("plain-password");
        when(emailReader.readMessages(account, "plain-password", account.getLastReadAt()))
                .thenThrow(new RuntimeException("IMAP indisponivel"));
        when(importHistoryRepository.save(any(EmailImportHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailImportResult result = importer.importAccount(account, USER_ID);

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.message()).isEqualTo("IMAP indisponivel");
        assertThat(account.getLastSyncStatus()).isEqualTo(EmailAccountStatus.FAILED);
        assertThat(account.getLastReadAt()).isEqualTo(Instant.parse("2026-07-07T11:00:00Z"));
        verify(emailAccountRepository).save(account);
        verify(leadRepository, never()).save(any());
        verify(emailReader, never()).markMessagesAsRead(any(), any(), any(), any());

        ArgumentCaptor<EmailImportHistory> importHistoryCaptor = ArgumentCaptor.forClass(EmailImportHistory.class);
        verify(importHistoryRepository).save(importHistoryCaptor.capture());
        assertThat(importHistoryCaptor.getValue().getStatus()).isEqualTo(EmailAccountStatus.FAILED);
        assertThat(importHistoryCaptor.getValue().getMessage()).isEqualTo("IMAP indisponivel");
        ArgumentCaptor<RuntimeException> exceptionCaptor = ArgumentCaptor.forClass(RuntimeException.class);
        verify(emailAccountFailureNotifier).notifyEmailAccountFailure(
                org.mockito.ArgumentMatchers.same(account),
                org.mockito.ArgumentMatchers.eq("Importacao de leads por e-mail"),
                exceptionCaptor.capture()
        );
        assertThat(exceptionCaptor.getValue().getMessage()).isEqualTo("IMAP indisponivel");
    }

    private EmailAccount account(Instant lastReadAt) {
        return new EmailAccount(
                UUID.fromString("00000000-0000-0000-0000-000000000501"),
                COMPANY_ID,
                STORE_ID,
                "Leads",
                "imap.example.com",
                993,
                "leads@example.com",
                "encrypted-password",
                EmailProtocol.IMAP,
                true,
                true,
                lastReadAt,
                Instant.parse("2026-07-07T10:00:00Z"),
                Instant.parse("2026-07-07T10:00:00Z"),
                EmailAccountStatus.NEVER_SYNCED,
                null,
                null
        );
    }

    private EmailMessage message(String body, String receivedAt) {
        return new EmailMessage("Novo lead", "portal@example.com", body, Instant.parse(receivedAt));
    }

    private ParsedEmailLead parsed(String name, String phone, String email, String vehicleInterest, String originalMessage) {
        return new ParsedEmailLead(name, phone, email, vehicleInterest, originalMessage, "EMAIL");
    }

    private Lead lead(UUID id, LeadStatus status) {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Lead(
                id,
                COMPANY_ID,
                STORE_ID,
                "Lead existente",
                "+5511999998888",
                "lead@example.com",
                null,
                "Honda Civic",
                LeadSource.WHATSAPP,
                "Lead anterior",
                status,
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
