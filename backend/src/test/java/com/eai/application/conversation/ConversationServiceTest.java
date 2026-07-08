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
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID OTHER_SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000302");

    private final WhatsAppContactRepository contactRepository = mock(WhatsAppContactRepository.class);
    private final ConversationRepository conversationRepository = mock(ConversationRepository.class);
    private final ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final ConversationService service = new ConversationService(contactRepository, conversationRepository, messageRepository, leadRepository);

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

    private void arrangeSummaryData(Conversation conversation, String leadName, String phone, String content, String messageAt, long unreadCount) {
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
                ConversationMessageStatus.RECEIVED,
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

    private AuthenticatedUser seller() {
        return new AuthenticatedUser(SELLER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }
}
