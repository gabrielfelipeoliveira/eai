package com.eai.application.message;

import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.conversation.ConversationService;
import com.eai.application.lead.LeadService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.lead.Lead;
import com.eai.domain.message.LeadCommunication;
import com.eai.domain.message.LeadCommunicationChannel;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateRepository templateRepository;
    private final LeadCommunicationRepository communicationRepository;
    private final LeadService leadService;
    private final CompanyService companyService;
    private final StoreService storeService;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    @Transactional(readOnly = true)
    public List<MessageTemplate> listTemplates(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return templateRepository.findAll();
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && authenticatedUser.storeId() == null) {
            return templateRepository.findByCompanyId(requireCompany(authenticatedUser));
        }
        return templateRepository.findByStoreScope(requireCompany(authenticatedUser), requireStore(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public List<MessageTemplate> listActiveTemplates(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return templateRepository.findActive();
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && authenticatedUser.storeId() == null) {
            return templateRepository.findActiveByCompanyId(requireCompany(authenticatedUser));
        }
        return templateRepository.findActiveByStoreScope(requireCompany(authenticatedUser), requireStore(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public MessageTemplate getTemplate(UUID id, AuthenticatedUser authenticatedUser) {
        MessageTemplate template = findRequired(id);
        assertCanAccessTemplate(template, authenticatedUser);
        return template;
    }

    @Transactional
    public MessageTemplate createTemplate(CreateMessageTemplateCommand command, AuthenticatedUser authenticatedUser) {
        validateTenant(command.companyId(), command.storeId());
        assertCanManageTenant(command.companyId(), command.storeId(), authenticatedUser);
        return templateRepository.save(MessageTemplate.create(
                command.companyId(),
                command.storeId(),
                command.name(),
                command.type(),
                command.content(),
                command.languageCode(),
                command.metaStatus(),
                command.active()
        ));
    }

    @Transactional
    public MessageTemplate updateTemplate(UUID id, UpdateMessageTemplateCommand command, AuthenticatedUser authenticatedUser) {
        MessageTemplate template = findRequired(id);
        assertCanAccessTemplate(template, authenticatedUser);
        validateTenant(command.companyId(), command.storeId());
        assertCanManageTenant(command.companyId(), command.storeId(), authenticatedUser);
        template.update(command.companyId(), command.storeId(), command.name(), command.type(), command.content(), command.languageCode(), command.metaStatus(), command.active());
        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(UUID id, AuthenticatedUser authenticatedUser) {
        MessageTemplate template = findRequired(id);
        assertCanAccessTemplate(template, authenticatedUser);
        template.softDelete();
        templateRepository.softDelete(template);
    }

    @Transactional
    public WhatsappLinkResult generateWhatsappLink(UUID leadId, UUID templateId, AuthenticatedUser authenticatedUser) {
        Lead lead = leadService.getLead(leadId, authenticatedUser);
        MessageTemplate template = findRequired(templateId);
        if (!canUseTemplateForLead(template, lead)) {
            throw new NotFoundException("Message template not found");
        }

        User seller = userRepository.findById(lead.getAssignedToUserId() == null ? authenticatedUser.id() : lead.getAssignedToUserId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));
        Store store = storeService.findRequired(lead.getStoreId());
        String message = MessageTemplateRenderer.render(template.getContent(), placeholders(lead, seller, store));
        String phone = toWhatsappPhone(lead.getCustomerPhone());
        String url = "https://wa.me/" + phone + "?text=" + UriUtils.encode(message, StandardCharsets.UTF_8);

        LeadCommunication communication = communicationRepository.save(LeadCommunication.create(
                lead.getId(),
                authenticatedUser.id(),
                LeadCommunicationChannel.WHATSAPP_LINK,
                template.getId(),
                message
        ));
        conversationService.recordOutboundMessage(lead, ConversationMessageType.TEMPLATE, ConversationMessageStatus.SENT, null, message, null);
        return new WhatsappLinkResult(lead.getId(), template.getId(), communication.getId(), message, url);
    }

    @Transactional(readOnly = true)
    public List<LeadCommunication> listLeadCommunications(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = leadService.getLead(leadId, authenticatedUser);
        return communicationRepository.findByLeadId(lead.getId());
    }

    private MessageTemplate findRequired(UUID id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message template not found"));
    }

    private void validateTenant(UUID companyId, UUID storeId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        companyService.findRequired(companyId);
        if (storeId == null) {
            return;
        }
        Store store = storeService.findRequired(storeId);
        if (!store.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
    }

    private void assertCanAccessTemplate(MessageTemplate template, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && template.getCompanyId().equals(requireCompany(authenticatedUser))) {
            if (authenticatedUser.storeId() == null || template.getStoreId() == null || template.getStoreId().equals(authenticatedUser.storeId())) {
                return;
            }
        }
        if (template.getStoreId() == null && template.getCompanyId().equals(requireCompany(authenticatedUser))) {
            return;
        }
        if (template.getStoreId() != null && template.getStoreId().equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for message template");
    }

    private void assertCanManageTenant(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(requireCompany(authenticatedUser))) {
            if (authenticatedUser.storeId() == null || storeId == null || storeId.equals(authenticatedUser.storeId())) {
                return;
            }
        }
        throw new ForbiddenException("Access denied for message template tenant");
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

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean canUseTemplateForLead(MessageTemplate template, Lead lead) {
        return template.isActive()
                && !template.isDeleted()
                && template.getMetaStatus() == MessageTemplateMetaStatus.APPROVED
                && template.getCompanyId().equals(lead.getCompanyId())
                && (template.getStoreId() == null || template.getStoreId().equals(lead.getStoreId()));
    }

    private Map<String, String> placeholders(Lead lead, User seller, Store store) {
        return Map.of(
                "cliente", valueOrEmpty(lead.getCustomerName()),
                "telefone", valueOrEmpty(lead.getCustomerPhone()),
                "veiculo", valueOrEmpty(vehicleDescription(lead)),
                "vendedor", valueOrEmpty(seller.getName()),
                "loja", valueOrEmpty(store.getName()),
                "cidade", valueOrEmpty(lead.getCustomerCity())
        );
    }

    private String toWhatsappPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Lead phone is required to generate WhatsApp link");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new IllegalArgumentException("Lead phone is required to generate WhatsApp link");
        }
        return digits;
    }

    private String vehicleDescription(Lead lead) {
        if (lead.getItem() == null || lead.getItem().getVehicle() == null) {
            return lead.getVehicleInterest();
        }
        String name = lead.getItem().getVehicle().getName();
        String model = lead.getItem().getVehicle().getModel();
        Integer year = lead.getItem().getVehicle().getYear();
        String description = String.join(" ", java.util.stream.Stream.of(name, model, year == null ? null : year.toString())
                .filter(value -> value != null && !value.isBlank())
                .toList());
        return description.isBlank() ? lead.getVehicleInterest() : description;
    }
}
