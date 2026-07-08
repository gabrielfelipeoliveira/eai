package com.eai.application.conversation;

import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.conversation.ConversationMessage;
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
        return conversationRepository.findByStoreId(requireStore(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public Conversation getConversation(UUID id, AuthenticatedUser authenticatedUser) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        assertCanAccess(conversation, authenticatedUser);
        return conversation;
    }

    @Transactional(readOnly = true)
    public List<ConversationMessage> listMessages(UUID conversationId, AuthenticatedUser authenticatedUser) {
        Conversation conversation = getConversation(conversationId, authenticatedUser);
        return messageRepository.findByConversationId(conversation.getId());
    }

    @Transactional(readOnly = true)
    public List<ConversationMessage> listLeadMessages(UUID leadId, AuthenticatedUser authenticatedUser) {
        Conversation conversation = conversationRepository.findByLeadId(leadId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        assertCanAccess(conversation, authenticatedUser);
        return messageRepository.findByConversationId(conversation.getId());
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
