package com.eai.application.whatsapp;

import com.eai.application.conversation.ConversationService;
import com.eai.application.lead.LeadService;
import com.eai.application.message.LeadCommunicationRepository;
import com.eai.application.message.MessageTemplateRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.message.LeadCommunication;
import com.eai.domain.message.LeadCommunicationChannel;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateType;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WhatsAppTemplateSenderServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TEMPLATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    private final MessageTemplateRepository templateRepository = mock(MessageTemplateRepository.class);
    private final LeadCommunicationRepository communicationRepository = mock(LeadCommunicationRepository.class);
    private final LeadService leadService = mock(LeadService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsAppChannelSettings settings = mock(WhatsAppChannelSettings.class);
    private final WhatsAppTemplateClient templateClient = mock(WhatsAppTemplateClient.class);

    private final WhatsAppTemplateSenderService service = new WhatsAppTemplateSenderService(
            templateRepository,
            communicationRepository,
            leadService,
            storeService,
            userRepository,
            conversationService,
            settings,
            templateClient
    );

    @DisplayName("Envia template e registra mensagem enviada com retorno do provedor")
    @Test
    @SuppressWarnings("unchecked")
    void sendsTemplateAndRecordsSentMessageWithProviderReturn() {
        Lead lead = arrangeBaseLead("+5511999990000");
        when(templateClient.sendTemplate(eq("5511999990000"), eq("primeiro_contato"), eq("pt_BR"), any()))
                .thenReturn(new WhatsAppTemplateProviderResult(true, 200, "wamid.123", "{\"messages\":[{\"id\":\"wamid.123\"}]}"));
        when(conversationService.recordOutboundMessage(eq(lead), eq(ConversationMessageType.TEMPLATE), eq(ConversationMessageStatus.SENT), eq("wamid.123"), any(), any()))
                .thenReturn(ConversationMessage.outbound(UUID.randomUUID(), ConversationMessageType.TEMPLATE, ConversationMessageStatus.SENT, "wamid.123", "Ola Cliente Teste, veja Honda Civic", "{\"messages\":[{\"id\":\"wamid.123\"}]}"));

        WhatsAppTemplateSendResult result = service.sendTemplate(lead.getId(), new SendWhatsAppTemplateCommand(TEMPLATE_ID, null), authenticatedUser());

        assertThat(result.status()).isEqualTo(ConversationMessageStatus.SENT);
        assertThat(result.externalMessageId()).isEqualTo("wamid.123");
        assertThat(result.providerResponse()).contains("wamid.123");

        ArgumentCaptor<List<String>> parameters = ArgumentCaptor.forClass(List.class);
        verify(templateClient).sendTemplate(eq("5511999990000"), eq("primeiro_contato"), eq("pt_BR"), parameters.capture());
        assertThat(parameters.getValue()).containsExactly("Cliente Teste", "Honda Civic");

        ArgumentCaptor<LeadCommunication> communication = ArgumentCaptor.forClass(LeadCommunication.class);
        verify(communicationRepository).save(communication.capture());
        assertThat(communication.getValue().getChannel()).isEqualTo(LeadCommunicationChannel.WHATSAPP_TEMPLATE);
    }

    @DisplayName("Registra mensagem com falha quando provedor rejeita envio")

    @Test
    void recordsFailedMessageWhenProviderRejectsSend() {
        Lead lead = arrangeBaseLead("+5511999990000");
        when(templateClient.sendTemplate(eq("5511999990000"), eq("primeiro_contato"), eq("en_US"), any()))
                .thenReturn(new WhatsAppTemplateProviderResult(false, 400, null, "{\"error\":{\"message\":\"Invalid template\"}}"));
        when(conversationService.recordOutboundMessage(eq(lead), eq(ConversationMessageType.TEMPLATE), eq(ConversationMessageStatus.FAILED), eq(null), any(), any()))
                .thenReturn(ConversationMessage.outbound(UUID.randomUUID(), ConversationMessageType.TEMPLATE, ConversationMessageStatus.FAILED, null, "Ola Cliente Teste, veja Honda Civic", "{\"error\":{\"message\":\"Invalid template\"}}"));

        WhatsAppTemplateSendResult result = service.sendTemplate(lead.getId(), new SendWhatsAppTemplateCommand(TEMPLATE_ID, "en_US"), authenticatedUser());

        assertThat(result.status()).isEqualTo(ConversationMessageStatus.FAILED);
        assertThat(result.providerResponse()).contains("Invalid template");
        verify(conversationService).recordOutboundMessage(
                eq(lead),
                eq(ConversationMessageType.TEMPLATE),
                eq(ConversationMessageStatus.FAILED),
                eq(null),
                any(),
                eq("{\"error\":{\"message\":\"Invalid template\"}}")
        );
    }

    private Lead arrangeBaseLead(String phone) {
        when(settings.templateSendingConfigured()).thenReturn(true);
        Lead lead = Lead.create(COMPANY_ID, STORE_ID, "Cliente Teste", phone, "cliente@eai.com", "Sao Paulo", "Honda Civic", LeadSource.MANUAL, null, null, null, null);
        MessageTemplate template = MessageTemplate.create(COMPANY_ID, STORE_ID, "primeiro_contato", MessageTemplateType.FIRST_CONTACT, "Ola {cliente}, veja {veiculo}", true);
        template.update(COMPANY_ID, STORE_ID, "primeiro_contato", MessageTemplateType.FIRST_CONTACT, "Ola {cliente}, veja {veiculo}", true);
        User user = User.create("Admin EAI", "admin@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, Set.of(UserRole.ADMIN));
        Store store = Store.create(COMPANY_ID, "EAI Motors Centro", "00000000000100", null, null, "Sao Paulo", "SP", null);

        when(leadService.getLead(lead.getId(), authenticatedUser())).thenReturn(lead);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(new MessageTemplate(
                TEMPLATE_ID,
                template.getCompanyId(),
                template.getStoreId(),
                template.getName(),
                template.getType(),
                template.getContent(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        )));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storeService.findRequired(STORE_ID)).thenReturn(store);
        when(communicationRepository.save(any(LeadCommunication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return lead;
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(USER_ID, "admin@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.ADMIN));
    }
}
