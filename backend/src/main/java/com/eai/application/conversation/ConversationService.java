package com.eai.application.conversation;

import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.conversation.WhatsAppContact;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private final WhatsAppContactRepository contactRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final LeadRepository leadRepository;

    public ConversationService(
            WhatsAppContactRepository contactRepository,
            ConversationRepository conversationRepository,
            ConversationMessageRepository messageRepository,
            LeadRepository leadRepository
    ) {
        this.contactRepository = contactRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.leadRepository = leadRepository;
    }

    @Transactional
    public Optional<ConversationMessage> recordIncomingMessage(UUID companyId, UUID storeId, IncomingWhatsAppMessage incomingMessage) {
        if (incomingMessage.externalMessageId() != null && messageRepository.existsByExternalMessageId(incomingMessage.externalMessageId())) {
            return Optional.empty();
        }
        Conversation conversation = findOrCreateConversation(companyId, storeId, incomingMessage.phone(), incomingMessage.contactName());
        ConversationMessage message = ConversationMessage.inbound(
                conversation.getId(),
                incomingMessage.type(),
                incomingMessage.externalMessageId(),
                incomingMessage.content(),
                incomingMessage.mediaId(),
                incomingMessage.mediaMimeType(),
                incomingMessage.rawPayload()
        );
        return Optional.of(messageRepository.save(message));
    }

    @Transactional
    public ConversationMessage recordOutboundMessage(Lead lead, ConversationMessageType type, ConversationMessageStatus status, String externalMessageId, String content, String rawPayload) {
        Conversation conversation = findOrCreateConversation(
                lead.getCompanyId(),
                lead.getStoreId(),
                lead.getCustomerPhone(),
                lead.getCustomerName()
        );
        conversation.linkLead(lead.getId(), lead.getAssignedToUserId());
        conversationRepository.save(conversation);
        ConversationMessage message = ConversationMessage.outbound(conversation.getId(), type, status, externalMessageId, content, rawPayload);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Conversation> listConversations(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return conversationRepository.findAll();
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && authenticatedUser.storeId() == null) {
            return conversationRepository.findByCompanyId(requireCompany(authenticatedUser));
        }
        if (hasRole(authenticatedUser, UserRole.SELLER)) {
            return conversationRepository.findByResponsibleUserId(authenticatedUser.id());
        }
        return conversationRepository.findByStoreId(requireStore(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public List<ConversationSummary> listConversationSummaries(AuthenticatedUser authenticatedUser) {
        return listConversations(authenticatedUser).stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(ConversationSummary::lastInteractionAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Conversation getConversation(UUID id, AuthenticatedUser authenticatedUser) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        assertCanAccess(conversation, authenticatedUser);
        return conversation;
    }

    @Transactional
    public List<ConversationMessage> listMessages(UUID conversationId, AuthenticatedUser authenticatedUser) {
        Conversation conversation = getConversation(conversationId, authenticatedUser);
        messageRepository.markInboundReceivedAsRead(conversation.getId());
        return messageRepository.findByConversationId(conversation.getId());
    }

    @Transactional
    public List<ConversationMessage> listLeadMessages(UUID leadId, AuthenticatedUser authenticatedUser) {
        Conversation conversation = conversationRepository.findByLeadId(leadId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        assertCanAccess(conversation, authenticatedUser);
        messageRepository.markInboundReceivedAsRead(conversation.getId());
        return messageRepository.findByConversationId(conversation.getId());
    }

    @Transactional
    public Optional<ConversationMessage> updateMessageStatusByExternalId(String externalMessageId, ConversationMessageStatus status) {
        return messageRepository.findByExternalMessageId(externalMessageId)
                .map(message -> {
                    if (shouldUpdateStatus(message.getStatus(), status)) {
                        message.updateStatus(status);
                    }
                    return messageRepository.save(message);
                });
    }

    private boolean shouldUpdateStatus(ConversationMessageStatus currentStatus, ConversationMessageStatus newStatus) {
        return newStatus == ConversationMessageStatus.FAILED || statusRank(newStatus) >= statusRank(currentStatus);
    }

    private int statusRank(ConversationMessageStatus status) {
        return switch (status) {
            case RECEIVED -> 0;
            case SENT -> 1;
            case DELIVERED -> 2;
            case READ -> 3;
            case FAILED -> 4;
        };
    }

    private ConversationSummary toSummary(Conversation conversation) {
        WhatsAppContact contact = contactRepository.findById(conversation.getContactId())
                .orElseThrow(() -> new NotFoundException("WhatsApp contact not found"));
        Lead lead = conversation.getLeadId() == null
                ? null
                : leadRepository.findById(conversation.getLeadId()).orElse(null);
        ConversationMessage lastMessage = messageRepository.findLatestByConversationId(conversation.getId()).orElse(null);
        long unreadCount = messageRepository.countByConversationIdAndDirectionAndStatus(
                conversation.getId(),
                ConversationMessageDirection.INBOUND,
                ConversationMessageStatus.RECEIVED
        );
        return new ConversationSummary(
                conversation.getId(),
                conversation.getCompanyId(),
                conversation.getStoreId(),
                conversation.getContactId(),
                conversation.getLeadId(),
                conversation.getResponsibleUserId(),
                lead == null ? null : lead.getCustomerName(),
                lead != null && lead.getCustomerPhone() != null ? lead.getCustomerPhone() : contact.getPhone(),
                contact.getDisplayName(),
                lastMessage == null ? null : lastMessage.getId(),
                lastMessage == null ? null : lastMessage.getDirection(),
                lastMessage == null ? null : lastMessage.getType(),
                lastMessage == null ? null : lastMessage.getStatus(),
                lastMessage == null ? null : lastMessage.getContent(),
                lastMessage == null ? conversation.getUpdatedAt() : lastMessage.getCreatedAt(),
                unreadCount,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private Conversation findOrCreateConversation(UUID companyId, UUID storeId, String phone, String contactName) {
        String normalizedPhone = normalizePhone(phone);
        Lead matchedLead = findLeadByPhone(storeId, normalizedPhone).orElse(null);
        UUID leadId = matchedLead == null ? null : matchedLead.getId();
        UUID responsibleUserId = matchedLead == null ? null : matchedLead.getAssignedToUserId();
        String displayName = contactName == null && matchedLead != null ? matchedLead.getCustomerName() : contactName;

        WhatsAppContact contact = contactRepository.findByStoreIdAndPhone(storeId, normalizedPhone)
                .orElseGet(() -> WhatsAppContact.create(companyId, storeId, leadId, normalizedPhone, displayName));
        contact.updateLead(leadId);
        contact.updateDisplayName(displayName);
        WhatsAppContact savedContact = contactRepository.save(contact);

        Conversation conversation = conversationRepository.findByContactId(savedContact.getId())
                .orElseGet(() -> Conversation.create(companyId, storeId, savedContact.getId(), savedContact.getLeadId(), responsibleUserId));
        conversation.linkLead(savedContact.getLeadId(), responsibleUserId);
        return conversationRepository.save(conversation);
    }

    private Optional<Lead> findLeadByPhone(UUID storeId, String normalizedPhone) {
        List<Lead> matches = leadRepository.findAll(new LeadSearchCriteria(null, null, null, storeId, null, null, null, null, null, null, null)).stream()
                .filter(lead -> phonesMatch(normalizedPhone, lead.getCustomerPhone()))
                .toList();
        return matches.stream()
                .filter(lead -> lead.getStatus() != LeadStatus.DUPLICATED)
                .max(Comparator.comparing(Lead::getCreatedAt))
                .or(() -> matches.stream().max(Comparator.comparing(Lead::getCreatedAt)));
    }

    private boolean phonesMatch(String normalizedPhone, String leadPhone) {
        String normalizedLeadPhone = normalizePhone(leadPhone);
        return normalizedPhone.equals(normalizedLeadPhone)
                || normalizedPhone.equals(stripBrazilCountryCode(normalizedLeadPhone))
                || stripBrazilCountryCode(normalizedPhone).equals(normalizedLeadPhone);
    }

    private String stripBrazilCountryCode(String phone) {
        if (phone != null && phone.startsWith("55") && phone.length() > 11) {
            return phone.substring(2);
        }
        return phone;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone is required");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new IllegalArgumentException("phone is required");
        }
        return digits;
    }

    private void assertCanAccess(Conversation conversation, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (conversation.getCompanyId().equals(requireCompany(authenticatedUser))) {
            if (hasRole(authenticatedUser, UserRole.MANAGER) && authenticatedUser.storeId() == null) {
                return;
            }
            if (hasRole(authenticatedUser, UserRole.SELLER)) {
                if (authenticatedUser.id().equals(conversation.getResponsibleUserId())) {
                    return;
                }
                throw new ForbiddenException("Access denied for conversation");
            }
            if (conversation.getStoreId().equals(requireStore(authenticatedUser))) {
                return;
            }
        }
        throw new ForbiddenException("Access denied for conversation");
    }

    private UUID requireCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() == null) {
            throw new ForbiddenException("User is not linked to a company");
        }
        return authenticatedUser.companyId();
    }

    private UUID requireStore(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.storeId() == null) {
            throw new ForbiddenException("User is not linked to a store");
        }
        return authenticatedUser.storeId();
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }
}
