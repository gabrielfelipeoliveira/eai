package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.NotFoundException;
import com.eai.application.conversation.ConversationService;
import com.eai.application.lead.LeadService;
import com.eai.application.message.LeadCommunicationRepository;
import com.eai.application.message.MessageTemplateRenderer;
import com.eai.application.message.MessageTemplateRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.lead.Lead;
import com.eai.domain.message.LeadCommunication;
import com.eai.domain.message.LeadCommunicationChannel;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhatsAppTemplateSenderService {

    private static final String DEFAULT_LANGUAGE_CODE = "pt-BR";

    private final MessageTemplateRepository templateRepository;
    private final LeadCommunicationRepository communicationRepository;
    private final LeadService leadService;
    private final StoreService storeService;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final WhatsAppChannelSettings settings;
    private final WhatsAppTemplateClient templateClient;

    @Transactional
    public WhatsAppTemplateSendResult sendTemplate(UUID leadId, SendWhatsAppTemplateCommand command, AuthenticatedUser authenticatedUser) {
        if (!settings.templateSendingConfigured()) {
            throw new ApplicationException("WHATSAPP_TEMPLATE_SENDING_NOT_CONFIGURED", "WhatsApp template sending is not configured");
        }
        Lead lead = leadService.getLead(leadId, authenticatedUser);
        MessageTemplate template = templateRepository.findById(command.templateId())
                .orElseThrow(() -> new NotFoundException("Message template not found"));
        if (!canUseTemplateForLead(template, lead)) {
            throw new NotFoundException("Message template not found");
        }

        User seller = userRepository.findById(lead.getAssignedToUserId() == null ? authenticatedUser.id() : lead.getAssignedToUserId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));
        Store store = storeService.findRequired(lead.getStoreId());
        Map<String, String> placeholders = Map.of(
                "cliente", valueOrEmpty(lead.getCustomerName()),
                "telefone", valueOrEmpty(lead.getCustomerPhone()),
                "veiculo", valueOrEmpty(lead.getVehicleInterest()),
                "vendedor", valueOrEmpty(seller.getName()),
                "loja", valueOrEmpty(store.getName()),
                "cidade", valueOrEmpty(lead.getCustomerCity())
        );
        String message = MessageTemplateRenderer.render(template.getContent(), placeholders);
        String phone = toWhatsappPhone(lead.getCustomerPhone());
        List<String> bodyParameters = resolveBodyParameters(template.getContent(), placeholders);

        WhatsAppTemplateProviderResult providerResult = templateClient.sendTemplate(
                phone,
                template.getName(),
                languageCode(command.languageCode(), template.getLanguageCode()),
                bodyParameters
        );
        ConversationMessageStatus status = providerResult.successful()
                ? ConversationMessageStatus.SENT
                : ConversationMessageStatus.FAILED;

        LeadCommunication communication = communicationRepository.save(LeadCommunication.create(
                lead.getId(),
                authenticatedUser.id(),
                LeadCommunicationChannel.WHATSAPP_TEMPLATE,
                template.getId(),
                message
        ));
        ConversationMessage conversationMessage = conversationService.recordOutboundMessage(
                lead,
                ConversationMessageType.TEMPLATE,
                status,
                providerResult.externalMessageId(),
                message,
                providerResult.rawResponse()
        );
        return new WhatsAppTemplateSendResult(
                lead.getId(),
                template.getId(),
                communication.getId(),
                conversationMessage.getId(),
                status,
                providerResult.externalMessageId(),
                message,
                providerResult.rawResponse()
        );
    }

    private List<String> resolveBodyParameters(String content, Map<String, String> placeholders) {
        return MessageTemplateRenderer.placeholderNamesInOrder(content).stream()
                .map(name -> valueOrEmpty(placeholders.get(name)))
                .toList();
    }

    private String languageCode(String requestedLanguageCode, String templateLanguageCode) {
        if (requestedLanguageCode != null && !requestedLanguageCode.isBlank()) {
            return requestedLanguageCode.trim();
        }
        if (templateLanguageCode != null && !templateLanguageCode.isBlank()) {
            return templateLanguageCode.trim();
        }
        return DEFAULT_LANGUAGE_CODE;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toWhatsappPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Lead phone is required to send WhatsApp template");
        }
        String digits = phone.replaceAll("\\D", "");
        return validatePhone(digits);
    }

    private String validatePhone(String phone) {
        if (phone.length() < 12 || phone.length() > 15) {
            throw new IllegalArgumentException("Lead phone is not valid for WhatsApp template sending");
        }
        return phone;
    }

    private boolean canUseTemplateForLead(MessageTemplate template, Lead lead) {
        return template.isActive()
                && !template.isDeleted()
                && template.getMetaStatus() == MessageTemplateMetaStatus.APPROVED
                && template.getCompanyId().equals(lead.getCompanyId())
                && (template.getStoreId() == null || template.getStoreId().equals(lead.getStoreId()));
    }
}
