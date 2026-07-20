package com.eai.application.message;

import com.eai.application.conversation.ConversationService;
import com.eai.application.lead.LeadService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.message.LeadCommunication;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
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

class MessageTemplateServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID TEMPLATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");

    private final MessageTemplateRepository templateRepository = mock(MessageTemplateRepository.class);
    private final LeadCommunicationRepository communicationRepository = mock(LeadCommunicationRepository.class);
    private final LeadService leadService = mock(LeadService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final MessageTemplateService service = new MessageTemplateService(
            templateRepository,
            communicationRepository,
            leadService,
            storeService,
            userRepository,
            conversationService
    );

    @DisplayName("Listagem da loja inclui templates globais da empresa")
    @Test
    void listTemplatesUsesCompanyAndStoreScope() {
        service.listTemplates(seller());

        verify(templateRepository).findByStoreScope(COMPANY_ID, STORE_ID);
    }

    @DisplayName("Exclusao de template e logica")
    @Test
    void deleteTemplateSoftDeletes() {
        MessageTemplate template = template(STORE_ID, MessageTemplateMetaStatus.APPROVED);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        service.deleteTemplate(TEMPLATE_ID, manager());

        ArgumentCaptor<MessageTemplate> templateCaptor = ArgumentCaptor.forClass(MessageTemplate.class);
        verify(templateRepository).softDelete(templateCaptor.capture());
        assertThat(templateCaptor.getValue().isActive()).isFalse();
        assertThat(templateCaptor.getValue().getDeletedAt()).isNotNull();
    }

    @DisplayName("Link de WhatsApp permite template global aprovado da empresa")
    @Test
    void generatesWhatsappLinkWithCompanyTemplate() {
        Lead lead = lead();
        MessageTemplate template = template(null, MessageTemplateMetaStatus.APPROVED);
        User seller = User.create("Ana Seller", "ana@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
        Store store = Store.create(COMPANY_ID, "EAI Motors Centro", "00000000000100", null, null, "Sao Paulo", "SP", null);

        when(leadService.getLead(lead.getId(), manager())).thenReturn(lead);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(seller));
        when(storeService.findRequired(STORE_ID)).thenReturn(store);
        when(communicationRepository.save(any(LeadCommunication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversationService.recordOutboundMessage(eq(lead), eq(ConversationMessageType.TEMPLATE), eq(ConversationMessageStatus.SENT), eq(null), any(), eq(null)))
                .thenReturn(ConversationMessage.outbound(UUID.randomUUID(), ConversationMessageType.TEMPLATE, ConversationMessageStatus.SENT, null, null, null));

        WhatsappLinkResult result = service.generateWhatsappLink(lead.getId(), TEMPLATE_ID, manager());

        assertThat(result.message()).contains("Cliente Teste", "Honda Civic", "Ana Seller", "EAI Motors Centro");
        assertThat(result.url()).contains("https://wa.me/5511999990000?text=");
    }

    private MessageTemplate template(UUID storeId, MessageTemplateMetaStatus metaStatus) {
        return new MessageTemplate(
                TEMPLATE_ID,
                COMPANY_ID,
                storeId,
                "primeiro_contato",
                MessageTemplateType.FIRST_CONTACT,
                "Ola {cliente}, veja {veiculo}. Aqui e {vendedor} da {loja}.",
                "pt-BR",
                metaStatus,
                true,
                Instant.parse("2026-07-07T10:00:00Z"),
                Instant.parse("2026-07-07T10:00:00Z"),
                null
        );
    }

    private Lead lead() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Lead(
                UUID.fromString("00000000-0000-0000-0000-000000000501"),
                COMPANY_ID,
                STORE_ID,
                "Cliente Teste",
                "+5511999990000",
                "cliente@eai.com",
                "Sao Paulo",
                "Honda Civic",
                LeadSource.WEBSITE,
                "Lead de teste",
                com.eai.domain.lead.LeadStatus.ASSIGNED,
                USER_ID,
                now,
                now,
                now,
                null,
                null,
                null,
                null
        );
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(USER_ID, "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
    }

    private AuthenticatedUser seller() {
        return new AuthenticatedUser(USER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }
}
