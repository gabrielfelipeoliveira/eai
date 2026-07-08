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
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WhatsAppTemplateSenderService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");
    private static final String DEFAULT_LANGUAGE_CODE = "pt_BR";

    private final MessageTemplateRepository templateRepository;
    private final LeadCommunicationRepository communicationRepository;
    private final LeadService leadService;
    private final StoreService storeService;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final WhatsAppChannelSettings settings;
    private final WhatsAppTemplateClient templateClient;

    public WhatsAppTemplateSenderService(
            MessageTemplateRepository templateRepository,
            LeadCommunicationRepository communicationRepository,
            LeadService leadService,
            StoreService storeService,
            UserRepository userRepository,
            ConversationService conversationService,
            WhatsAppChannelSettings settings,
            WhatsAppTemplateClient templateClient
    ) {
        this.templateRepository = templateRepository;
        this.communicationRepository = communicationRepository;
        this.leadService = leadService;
        this.storeService = storeService;
        this.userRepository = userRepository;
        this.conversationService = conversationService;
        this.settings = settings;
        this.templateClient = templateClient;
    }

    @Transactional
    public WhatsAppTemplateSendResult sendTemplate(UUID leadId, SendWhatsAppTemplateCommand command, AuthenticatedUser authenticatedUser) {
        if (!settings.templateSendingConfigured()) {
            throw new ApplicationException("WHATSAPP_TEMPLATE_SENDING_NOT_CONFIGURED", "WhatsApp template sending is not configured");
        }
        Lead lead = leadService.getLead(leadId, authenticatedUser);
        MessageTemplate template = templateRepository.findById(command.templateId())
                .orElseThrow(() -> new NotFoundException("Message template not found"));
        if (!template.isActive() || !template.getCompanyId().equals(lead.getCompanyId()) || !template.getStoreId().equals(lead.getStoreId())) {
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
                languageCode(command.languageCode()),
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
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        LinkedHashSet<String> names = new LinkedHashSet<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        List<String> parameters = new ArrayList<>();
        for (String name : names) {
            parameters.add(valueOrEmpty(placeholders.get(name)));
        }
        return parameters;
    }

    private String languageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return DEFAULT_LANGUAGE_CODE;
        }
        return languageCode.trim();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toWhatsappPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Lead phone is required to send WhatsApp template");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("55") && digits.length() > 11) {
            return validatePhone(digits);
        }
        return validatePhone("55" + digits);
    }

    private String validatePhone(String phone) {
        if (phone.length() < 12 || phone.length() > 15) {
            throw new IllegalArgumentException("Lead phone is not valid for WhatsApp template sending");
        }
        return phone;
    }
}
