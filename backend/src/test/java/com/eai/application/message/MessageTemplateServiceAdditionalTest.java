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
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.message.LeadCommunication;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageTemplateServiceAdditionalTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID OTHER_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID TEMPLATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID LEAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final Instant NOW = Instant.parse("2026-07-27T10:00:00Z");

    private final MessageTemplateRepository templateRepository = mock(MessageTemplateRepository.class);
    private final LeadCommunicationRepository communicationRepository = mock(LeadCommunicationRepository.class);
    private final LeadService leadService = mock(LeadService.class);
    private final CompanyService companyService = mock(CompanyService.class);
    private final StoreService storeService = mock(StoreService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final MessageTemplateService service = new MessageTemplateService(
            templateRepository,
            communicationRepository,
            leadService,
            companyService,
            storeService,
            userRepository,
            conversationService
    );

    @DisplayName("Listagens usam repositorio correto por papel e escopo")
    @Test
    void listTemplatesUsesRoleSpecificRepositories() {
        service.listTemplates(admin());
        service.listTemplates(companyManager());
        service.listActiveTemplates(admin());
        service.listActiveTemplates(companyManager());
        service.listActiveTemplates(seller());

        verify(templateRepository).findAll();
        verify(templateRepository).findByCompanyId(COMPANY_ID);
        verify(templateRepository).findActive();
        verify(templateRepository).findActiveByCompanyId(COMPANY_ID);
        verify(templateRepository).findActiveByStoreScope(COMPANY_ID, STORE_ID);
    }

    @DisplayName("Busca template respeita acesso de gerente vendedor e admin")
    @Test
    void getTemplateAppliesAccessRules() {
        MessageTemplate global = template(null, MessageTemplateMetaStatus.APPROVED, true, null);
        MessageTemplate storeTemplate = template(STORE_ID, MessageTemplateMetaStatus.APPROVED, true, null);
        when(templateRepository.findById(TEMPLATE_ID))
                .thenReturn(Optional.of(global))
                .thenReturn(Optional.of(storeTemplate))
                .thenReturn(Optional.of(storeTemplate));

        assertThat(service.getTemplate(TEMPLATE_ID, seller())).isSameAs(global);
        assertThat(service.getTemplate(TEMPLATE_ID, manager())).isSameAs(storeTemplate);
        assertThat(service.getTemplate(TEMPLATE_ID, admin())).isSameAs(storeTemplate);
    }

    @DisplayName("Bloqueia acesso a template de outra loja")
    @Test
    void getTemplateRejectsOtherStore() {
        when(templateRepository.findById(TEMPLATE_ID))
                .thenReturn(Optional.of(template(OTHER_STORE_ID, MessageTemplateMetaStatus.APPROVED, true, null)));

        assertThatThrownBy(() -> service.getTemplate(TEMPLATE_ID, seller()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Atualiza template validando loja da empresa")
    @Test
    void updateTemplateValidatesTenantAndPersistsChanges() {
        MessageTemplate template = template(STORE_ID, MessageTemplateMetaStatus.PENDING, false, null);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, COMPANY_ID));
        when(templateRepository.save(any(MessageTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0, MessageTemplate.class));

        MessageTemplate result = service.updateTemplate(TEMPLATE_ID, new UpdateMessageTemplateCommand(
                COMPANY_ID,
                STORE_ID,
                "follow_up",
                MessageTemplateType.FOLLOW_UP,
                "Ola {cliente}",
                "pt-BR",
                MessageTemplateMetaStatus.APPROVED,
                true
        ), manager());

        assertThat(result.getName()).isEqualTo("follow_up");
        assertThat(result.getType()).isEqualTo(MessageTemplateType.FOLLOW_UP);
        assertThat(result.getMetaStatus()).isEqualTo(MessageTemplateMetaStatus.APPROVED);
        assertThat(result.isActive()).isTrue();
    }

    @DisplayName("Criacao rejeita loja de outra empresa")
    @Test
    void createTemplateRejectsStoreFromAnotherCompany() {
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, OTHER_COMPANY_ID));

        assertThatThrownBy(() -> service.createTemplate(new CreateMessageTemplateCommand(
                COMPANY_ID,
                STORE_ID,
                "primeiro_contato",
                MessageTemplateType.FIRST_CONTACT,
                "Ola",
                "pt-BR",
                MessageTemplateMetaStatus.APPROVED,
                true
        ), manager())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("store does not belong");
    }

    @DisplayName("Link WhatsApp rejeita template inativo excluido pendente ou de outra loja")
    @Test
    void whatsappLinkRejectsUnavailableTemplates() {
        Lead lead = lead("+5511999990000", STORE_ID, USER_ID);
        when(leadService.getLead(LEAD_ID, manager())).thenReturn(lead);

        List<MessageTemplate> unavailableTemplates = List.of(
                template(null, MessageTemplateMetaStatus.APPROVED, false, null),
                template(null, MessageTemplateMetaStatus.PENDING, true, null),
                template(OTHER_STORE_ID, MessageTemplateMetaStatus.APPROVED, true, null),
                template(null, MessageTemplateMetaStatus.APPROVED, true, NOW)
        );
        for (MessageTemplate template : unavailableTemplates) {
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

            assertThatThrownBy(() -> service.generateWhatsappLink(LEAD_ID, TEMPLATE_ID, manager()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @DisplayName("Link WhatsApp usa usuario autenticado quando lead nao tem vendedor")
    @Test
    void whatsappLinkUsesAuthenticatedUserWhenLeadHasNoSeller() {
        Lead lead = lead("+55 (11) 99999-0000", STORE_ID, null);
        MessageTemplate template = template(STORE_ID, MessageTemplateMetaStatus.APPROVED, true, null);
        User seller = User.create("Gerente", "gerente@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
        Store store = store(STORE_ID, COMPANY_ID);
        when(leadService.getLead(LEAD_ID, manager())).thenReturn(lead);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(seller));
        when(storeService.findRequired(STORE_ID)).thenReturn(store);
        when(communicationRepository.save(any(LeadCommunication.class))).thenAnswer(invocation -> invocation.getArgument(0, LeadCommunication.class));

        WhatsappLinkResult result = service.generateWhatsappLink(LEAD_ID, TEMPLATE_ID, manager());

        assertThat(result.url()).contains("https://wa.me/5511999990000?text=");
        verify(conversationService).recordOutboundMessage(eq(lead), eq(ConversationMessageType.TEMPLATE), eq(ConversationMessageStatus.SENT), isNull(String.class), anyString(), isNull(String.class));
    }

    @DisplayName("Link WhatsApp rejeita telefone ausente")
    @Test
    void whatsappLinkRejectsMissingPhone() {
        Lead lead = lead(null, STORE_ID, USER_ID);
        when(leadService.getLead(LEAD_ID, manager())).thenReturn(lead);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template(STORE_ID, MessageTemplateMetaStatus.APPROVED, true, null)));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.create("Seller", "seller@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER))));
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, COMPANY_ID));

        assertThatThrownBy(() -> service.generateWhatsappLink(LEAD_ID, TEMPLATE_ID, manager()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lead phone");
    }

    @DisplayName("Lista comunicacoes do lead apos validar acesso")
    @Test
    void listLeadCommunicationsUsesLeadServiceScope() {
        Lead lead = lead("+5511999990000", STORE_ID, USER_ID);
        LeadCommunication communication = LeadCommunication.create(LEAD_ID, USER_ID, com.eai.domain.message.LeadCommunicationChannel.WHATSAPP_LINK, TEMPLATE_ID, "Ola");
        when(leadService.getLead(LEAD_ID, manager())).thenReturn(lead);
        when(communicationRepository.findByLeadId(LEAD_ID)).thenReturn(List.of(communication));

        List<LeadCommunication> result = service.listLeadCommunications(LEAD_ID, manager());

        assertThat(result).containsExactly(communication);
    }

    private MessageTemplate template(UUID storeId, MessageTemplateMetaStatus metaStatus, boolean active, Instant deletedAt) {
        return new MessageTemplate(
                TEMPLATE_ID,
                COMPANY_ID,
                storeId,
                "primeiro_contato",
                MessageTemplateType.FIRST_CONTACT,
                "Ola {cliente}, aqui e {vendedor} da {loja}. Veiculo: {veiculo}. Cidade: {cidade}.",
                "pt-BR",
                metaStatus,
                active,
                NOW,
                NOW,
                deletedAt
        );
    }

    private Lead lead(String phone, UUID storeId, UUID sellerId) {
        return new Lead(
                LEAD_ID,
                COMPANY_ID,
                storeId,
                "Cliente Teste",
                phone,
                "cliente@eai.com",
                "Sao Paulo",
                "Honda Civic",
                LeadSource.WEBSITE,
                "Lead",
                sellerId == null ? LeadStatus.NEW : LeadStatus.ASSIGNED,
                sellerId,
                sellerId == null ? null : NOW,
                NOW,
                NOW,
                null,
                null,
                null,
                null
        );
    }

    private Store store(UUID storeId, UUID companyId) {
        return new Store(storeId, companyId, "Loja", "00000000000100", null, null, "Sao Paulo", "SP", null, com.eai.domain.tenant.TenantStatus.ACTIVE, NOW, NOW);
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(USER_ID, "admin@eai.com", null, null, Set.of(UserRole.ADMIN));
    }

    private AuthenticatedUser companyManager() {
        return new AuthenticatedUser(USER_ID, "manager@eai.com", COMPANY_ID, null, Set.of(UserRole.MANAGER));
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(USER_ID, "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
    }

    private AuthenticatedUser seller() {
        return new AuthenticatedUser(USER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }
}
