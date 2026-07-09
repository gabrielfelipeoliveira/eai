package com.eai.application.conversation;

import com.eai.application.lead.LeadRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.conversation.WhatsAppContact;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID OTHER_SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000302");

    private final WhatsAppContactRepository contactRepository = mock(WhatsAppContactRepository.class);
    private final ConversationRepository conversationRepository = mock(ConversationRepository.class);
    private final ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
    private final ConversationMessageEventRepository messageEventRepository = mock(ConversationMessageEventRepository.class);
    private final ConversationAccessAuditRepository accessAuditRepository = mock(ConversationAccessAuditRepository.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final ConversationService service = new ConversationService(contactRepository, conversationRepository, messageRepository, messageEventRepository, accessAuditRepository, leadRepository);

    @Test
    void listsSellerSummariesOrderedByLatestInteractionWithUnreadCount() {
        Conversation older = conversation("00000000-0000-0000-0000-000000000401", "00000000-0000-0000-0000-000000000501", "00000000-0000-0000-0000-000000000601", SELLER_ID);
        Conversation newer = conversation("00000000-0000-0000-0000-000000000402", "00000000-0000-0000-0000-000000000502", "00000000-0000-0000-0000-000000000602", SELLER_ID);

        when(conversationRepository.findByResponsibleUserId(SELLER_ID)).thenReturn(List.of(older, newer));
        arrangeSummaryData(older, "Mariana Alves", "5511988880001", "Mensagem antiga", "2026-07-08T12:00:00Z", 1);
        arrangeSummaryData(newer, "Bruno Costa", "5511977770002", "Mensagem recente", "2026-07-08T13:00:00Z", 3);

        List<ConversationSummary> summaries = service.listConversationSummaries(seller());

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).id()).isEqualTo(newer.getId());
        assertThat(summaries.get(0).leadName()).isEqualTo("Bruno Costa");
        assertThat(summaries.get(0).lastMessageContent()).isEqualTo("Mensagem recente");
        assertThat(summaries.get(0).unreadCount()).isEqualTo(3);
        assertThat(summaries.get(1).id()).isEqualTo(older.getId());
    }

    @Test
    void sellerListUsesResponsibleUserScope() {
        when(conversationRepository.findByResponsibleUserId(SELLER_ID)).thenReturn(List.of());
        when(conversationRepository.findByResponsibleUserId(OTHER_SELLER_ID)).thenReturn(List.of(
                conversation("00000000-0000-0000-0000-000000000403", "00000000-0000-0000-0000-000000000503", "00000000-0000-0000-0000-000000000603", OTHER_SELLER_ID)
        ));

        assertThat(service.listConversations(seller())).isEmpty();
    }

    @Test
    void filtersSummariesBySellerStatusAndPeriodForManager() {
        Conversation matching = conversation("00000000-0000-0000-0000-000000000403", "00000000-0000-0000-0000-000000000503", "00000000-0000-0000-0000-000000000603", SELLER_ID);
        Conversation otherSeller = conversation("00000000-0000-0000-0000-000000000405", "00000000-0000-0000-0000-000000000505", "00000000-0000-0000-0000-000000000605", OTHER_SELLER_ID);
        Conversation otherStatus = conversation("00000000-0000-0000-0000-000000000406", "00000000-0000-0000-0000-000000000506", "00000000-0000-0000-0000-000000000606", SELLER_ID);

        when(conversationRepository.findByStoreId(STORE_ID)).thenReturn(List.of(matching, otherSeller, otherStatus));
        arrangeSummaryData(matching, "Mariana Alves", "5511988880001", "Mensagem", "2026-07-08T12:00:00Z", 1, ConversationMessageStatus.RECEIVED);
        arrangeSummaryData(otherSeller, "Bruno Costa", "5511977770002", "Mensagem", "2026-07-08T12:30:00Z", 0, ConversationMessageStatus.RECEIVED);
        arrangeSummaryData(otherStatus, "Carla Lima", "5511966660003", "Mensagem", "2026-07-08T13:00:00Z", 0, ConversationMessageStatus.READ);

        ConversationFilters filters = new ConversationFilters(
                SELLER_ID,
                ConversationMessageStatus.RECEIVED,
                Instant.parse("2026-07-08T11:00:00Z"),
                Instant.parse("2026-07-08T12:15:00Z")
        );

        List<ConversationSummary> summaries = service.listConversationSummaries(manager(), filters);

        assertThat(summaries).extracting(ConversationSummary::id).containsExactly(matching.getId());
    }

    @Test
    void sellerFilterCannotExpandSellerScope() {
        Conversation own = conversation("00000000-0000-0000-0000-000000000407", "00000000-0000-0000-0000-000000000507", "00000000-0000-0000-0000-000000000607", SELLER_ID);
        when(conversationRepository.findByResponsibleUserId(SELLER_ID)).thenReturn(List.of(own));
        arrangeSummaryData(own, "Mariana Alves", "5511988880001", "Mensagem", "2026-07-08T12:00:00Z", 0, ConversationMessageStatus.RECEIVED);

        ConversationFilters filters = new ConversationFilters(OTHER_SELLER_ID, null, null, null);

        List<ConversationSummary> summaries = service.listConversationSummaries(seller(), filters);

        assertThat(summaries).extracting(ConversationSummary::id).containsExactly(own.getId());
    }

    @Test
    void marksInboundReceivedMessagesAsReadWhenListingMessages() {
        Conversation conversation = conversation("00000000-0000-0000-0000-000000000404", "00000000-0000-0000-0000-000000000504", "00000000-0000-0000-0000-000000000604", SELLER_ID);
        ConversationMessage older = message(conversation.getId(), ConversationMessageDirection.INBOUND, ConversationMessageStatus.READ, "Ola", "2026-07-08T11:00:00Z");
        ConversationMessage newer = message(conversation.getId(), ConversationMessageDirection.OUTBOUND, ConversationMessageStatus.SENT, "Bom dia", "2026-07-08T11:02:00Z");

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationId(conversation.getId())).thenReturn(List.of(older, newer));

        List<ConversationMessage> messages = service.listMessages(conversation.getId(), seller());

        verify(messageRepository).markInboundReceivedAsRead(conversation.getId());
        verify(accessAuditRepository, never()).save(org.mockito.ArgumentMatchers.any());
        assertThat(messages).containsExactly(older, newer);
    }

    @Test
    void recordsManagerAccessWhenListingMessages() {
        Conversation conversation = conversation("00000000-0000-0000-0000-000000000408", "00000000-0000-0000-0000-000000000508", "00000000-0000-0000-0000-000000000608", SELLER_ID);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationId(conversation.getId())).thenReturn(List.of());

        service.listMessages(conversation.getId(), manager());

        verify(accessAuditRepository).save(org.mockito.ArgumentMatchers.argThat(audit ->
                conversation.getId().equals(audit.getConversationId())
                        && audit.getActorUserId().equals(manager().id())
                        && audit.getActorRole() == UserRole.MANAGER
                        && "LIST_MESSAGES".equals(audit.getAccessType())
        ));
    }

    @Test
    void updatesOutboundMessageStatusByExternalId() {
        ConversationMessage message = message(UUID.randomUUID(), ConversationMessageDirection.OUTBOUND, ConversationMessageStatus.SENT, "Bom dia", "2026-07-08T11:02:00Z");
        when(messageRepository.findByExternalMessageId("wamid.123")).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        Optional<ConversationMessage> updated = service.updateMessageStatusByExternalId("wamid.123", ConversationMessageStatus.DELIVERED);

        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(ConversationMessageStatus.DELIVERED);
        verify(messageEventRepository).save(org.mockito.ArgumentMatchers.argThat(event ->
                message.getId().equals(event.getMessageId())
                        && "wamid.123".equals(event.getExternalMessageId())
                        && event.getStatus() == ConversationMessageStatus.DELIVERED
        ));
        verify(messageRepository).save(message);
    }

    @Test
    void doesNotDowngradeReadStatusWhenDelayedProviderEventArrives() {
        ConversationMessage message = message(UUID.randomUUID(), ConversationMessageDirection.OUTBOUND, ConversationMessageStatus.READ, "Bom dia", "2026-07-08T11:02:00Z");
        when(messageRepository.findByExternalMessageId("wamid.123")).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        Optional<ConversationMessage> updated = service.updateMessageStatusByExternalId("wamid.123", ConversationMessageStatus.DELIVERED);

        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(ConversationMessageStatus.READ);
    }

    @Test
    void recordsFailedStatusEventWithReason() {
        ConversationMessage message = message(UUID.randomUUID(), ConversationMessageDirection.OUTBOUND, ConversationMessageStatus.SENT, "Bom dia", "2026-07-08T11:02:00Z");
        when(messageRepository.findByExternalMessageId("wamid.456")).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        Optional<ConversationMessage> updated = service.recordMessageStatusEvent(
                "wamid.456",
                ConversationMessageStatus.FAILED,
                "Recipient phone number is invalid",
                "{\"status\":\"failed\"}",
                Instant.parse("2026-07-08T11:03:00Z")
        );

        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(ConversationMessageStatus.FAILED);
        verify(messageEventRepository).save(org.mockito.ArgumentMatchers.argThat(event ->
                message.getId().equals(event.getMessageId())
                        && event.getStatus() == ConversationMessageStatus.FAILED
                        && "Recipient phone number is invalid".equals(event.getFailureReason())
                        && "{\"status\":\"failed\"}".equals(event.getRawPayload())
                        && Instant.parse("2026-07-08T11:03:00Z").equals(event.getOccurredAt())
        ));
    }

    @Test
    void recordsStatusEventEvenWhenMessageWasNotFound() {
        when(messageRepository.findByExternalMessageId("wamid.unknown")).thenReturn(Optional.empty());

        Optional<ConversationMessage> updated = service.recordMessageStatusEvent(
                "wamid.unknown",
                ConversationMessageStatus.READ,
                null,
                "{\"status\":\"read\"}",
                Instant.parse("2026-07-08T11:04:00Z")
        );

        assertThat(updated).isEmpty();
        verify(messageEventRepository).save(org.mockito.ArgumentMatchers.argThat(event ->
                event.getMessageId() == null
                        && "wamid.unknown".equals(event.getExternalMessageId())
                        && event.getStatus() == ConversationMessageStatus.READ
        ));
    }

    private void arrangeSummaryData(Conversation conversation, String leadName, String phone, String content, String messageAt, long unreadCount) {
        arrangeSummaryData(conversation, leadName, phone, content, messageAt, unreadCount, ConversationMessageStatus.RECEIVED);
    }

    private void arrangeSummaryData(Conversation conversation, String leadName, String phone, String content, String messageAt, long unreadCount, ConversationMessageStatus status) {
        WhatsAppContact contact = new WhatsAppContact(conversation.getContactId(), COMPANY_ID, STORE_ID, conversation.getLeadId(), phone, leadName, Instant.parse("2026-07-08T10:00:00Z"), Instant.parse("2026-07-08T10:00:00Z"));
        Lead lead = new Lead(
                conversation.getLeadId(),
                COMPANY_ID,
                STORE_ID,
                leadName,
                phone,
                null,
                null,
                "Honda Civic",
                LeadSource.MANUAL,
                null,
                LeadStatus.ASSIGNED,
                SELLER_ID,
                Instant.parse("2026-07-08T10:00:00Z"),
                Instant.parse("2026-07-08T10:00:00Z"),
                Instant.parse("2026-07-08T10:00:00Z"),
                null,
                null,
                null,
                null
        );
        ConversationMessage message = new ConversationMessage(
                UUID.randomUUID(),
                conversation.getId(),
                ConversationMessageDirection.INBOUND,
                ConversationMessageType.TEXT,
                status,
                null,
                content,
                null,
                null,
                null,
                Instant.parse(messageAt),
                Instant.parse(messageAt)
        );

        when(contactRepository.findById(conversation.getContactId())).thenReturn(Optional.of(contact));
        when(leadRepository.findById(conversation.getLeadId())).thenReturn(Optional.of(lead));
        when(messageRepository.findLatestByConversationId(conversation.getId())).thenReturn(Optional.of(message));
        when(messageRepository.countByConversationIdAndDirectionAndStatus(conversation.getId(), ConversationMessageDirection.INBOUND, ConversationMessageStatus.RECEIVED)).thenReturn(unreadCount);
    }

    private Conversation conversation(String id, String contactId, String leadId, UUID sellerId) {
        return new Conversation(
                UUID.fromString(id),
                COMPANY_ID,
                STORE_ID,
                UUID.fromString(contactId),
                UUID.fromString(leadId),
                sellerId,
                Instant.parse("2026-07-08T10:00:00Z"),
                Instant.parse("2026-07-08T10:00:00Z")
        );
    }

    private ConversationMessage message(UUID conversationId, ConversationMessageDirection direction, ConversationMessageStatus status, String content, String createdAt) {
        return new ConversationMessage(
                UUID.randomUUID(),
                conversationId,
                direction,
                ConversationMessageType.TEXT,
                status,
                null,
                content,
                null,
                null,
                null,
                Instant.parse(createdAt),
                Instant.parse(createdAt)
        );
    }

    private AuthenticatedUser seller() {
        return new AuthenticatedUser(SELLER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(UUID.fromString("00000000-0000-0000-0000-000000000701"), "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
    }
}
