package com.eai.api.metadata;

import com.eai.domain.distribution.LeadDistributionMode;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;
import com.eai.domain.lead.FollowUpTaskStatus;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.message.MessageTemplateType;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    @GetMapping
    public MetadataResponse metadata(@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        String locale = resolveLocale(acceptLanguage);
        return new MetadataResponse(
                locale,
                leadStatuses(),
                leadSources(),
                followUpStatuses(),
                userRoles(),
                userStatuses(),
                tenantStatuses(),
                messageTemplateTypes(),
                leadDistributionModes(),
                emailAccountStatuses(),
                emailProtocols(),
                conversationMessageDirections(),
                conversationMessageTypes(),
                conversationMessageStatuses()
        );
    }

    private String resolveLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "pt-BR";
        }
        return acceptLanguage.toLowerCase().startsWith("pt") ? "pt-BR" : "pt-BR";
    }

    private List<MetadataOptionResponse> leadStatuses() {
        return List.of(
                option(LeadStatus.NEW, "lead.status.new", "Novo", 1, "info"),
                option(LeadStatus.AVAILABLE, "lead.status.available", "Disponivel", 2, "primary"),
                option(LeadStatus.ASSIGNED, "lead.status.assigned", "Atribuido", 3, "secondary"),
                option(LeadStatus.FIRST_CONTACT, "lead.status.first_contact", "Primeiro contato", 4, "warning"),
                option(LeadStatus.IN_NEGOTIATION, "lead.status.in_negotiation", "Em negociacao", 5, "warning"),
                option(LeadStatus.VISIT_SCHEDULED, "lead.status.visit_scheduled", "Visita agendada", 6, "info"),
                option(LeadStatus.PROPOSAL_SENT, "lead.status.proposal_sent", "Proposta enviada", 7, "secondary"),
                option(LeadStatus.SOLD, "lead.status.sold", "Vendido", 8, "success"),
                option(LeadStatus.LOST, "lead.status.lost", "Perdido", 9, "error"),
                option(LeadStatus.DUPLICATED, "lead.status.duplicated", "Duplicado", 10, "default")
        );
    }

    private List<MetadataOptionResponse> leadSources() {
        return List.of(
                option(LeadSource.MANUAL, "lead.source.manual", "Manual", 1, "default"),
                option(LeadSource.EMAIL, "lead.source.email", "E-mail", 2, "default"),
                option(LeadSource.WEBSITE, "lead.source.website", "Site", 3, "default"),
                option(LeadSource.FACEBOOK, "lead.source.facebook", "Facebook", 4, "default"),
                option(LeadSource.INSTAGRAM, "lead.source.instagram", "Instagram", 5, "default"),
                option(LeadSource.WEBMOTORS, "lead.source.webmotors", "Webmotors", 6, "default"),
                option(LeadSource.ICARROS, "lead.source.icarros", "iCarros", 7, "default"),
                option(LeadSource.OLX, "lead.source.olx", "OLX", 8, "default"),
                option(LeadSource.API, "lead.source.api", "API", 9, "default")
        );
    }

    private List<MetadataOptionResponse> followUpStatuses() {
        return List.of(
                option(FollowUpTaskStatus.PENDING, "follow_up.status.pending", "Pendente", 1, "warning"),
                option(FollowUpTaskStatus.DONE, "follow_up.status.done", "Concluido", 2, "success"),
                option(FollowUpTaskStatus.CANCELED, "follow_up.status.canceled", "Cancelado", 3, "default"),
                option(FollowUpTaskStatus.OVERDUE, "follow_up.status.overdue", "Atrasado", 4, "error")
        );
    }

    private List<MetadataOptionResponse> userRoles() {
        return List.of(
                option(UserRole.ADMIN, "user.role.admin", "Administrador", 1, "error"),
                option(UserRole.MANAGER, "user.role.manager", "Gerente", 2, "primary"),
                option(UserRole.SELLER, "user.role.seller", "Vendedor", 3, "success"),
                option(UserRole.RECEPTIONIST, "user.role.receptionist", "Recepcao", 4, "info"),
                option(UserRole.AUDITOR, "user.role.auditor", "Auditoria", 5, "secondary")
        );
    }

    private List<MetadataOptionResponse> userStatuses() {
        return List.of(
                option(UserStatus.ACTIVE, "user.status.active", "Ativo", 1, "success"),
                option(UserStatus.INACTIVE, "user.status.inactive", "Inativo", 2, "default")
        );
    }

    private List<MetadataOptionResponse> tenantStatuses() {
        return List.of(
                option(TenantStatus.ACTIVE, "tenant.status.active", "Ativo", 1, "success"),
                option(TenantStatus.INACTIVE, "tenant.status.inactive", "Inativo", 2, "default")
        );
    }

    private List<MetadataOptionResponse> messageTemplateTypes() {
        return List.of(
                option(MessageTemplateType.FIRST_CONTACT, "message_template.type.first_contact", "Primeiro contato", 1, "info"),
                option(MessageTemplateType.FOLLOW_UP, "message_template.type.follow_up", "Follow-up", 2, "warning"),
                option(MessageTemplateType.VISIT_INVITE, "message_template.type.visit_invite", "Convite para visita", 3, "info"),
                option(MessageTemplateType.PROPOSAL, "message_template.type.proposal", "Proposta", 4, "secondary"),
                option(MessageTemplateType.NO_RESPONSE, "message_template.type.no_response", "Sem resposta", 5, "default"),
                option(MessageTemplateType.SOLD, "message_template.type.sold", "Venda concluida", 6, "success"),
                option(MessageTemplateType.LOST, "message_template.type.lost", "Lead perdido", 7, "error")
        );
    }

    private List<MetadataOptionResponse> leadDistributionModes() {
        return List.of(
                option(LeadDistributionMode.MANUAL, "lead_distribution.mode.manual", "Manual", 1, "default"),
                option(LeadDistributionMode.ROUND_ROBIN, "lead_distribution.mode.round_robin", "Rodizio", 2, "primary"),
                option(LeadDistributionMode.LEAST_BUSY, "lead_distribution.mode.least_busy", "Menor carteira", 3, "secondary")
        );
    }

    private List<MetadataOptionResponse> emailAccountStatuses() {
        return List.of(
                option(EmailAccountStatus.NEVER_SYNCED, "email_account.status.never_synced", "Nunca sincronizada", 1, "default"),
                option(EmailAccountStatus.SUCCESS, "email_account.status.success", "Sincronizada", 2, "success"),
                option(EmailAccountStatus.FAILED, "email_account.status.failed", "Falhou", 3, "error")
        );
    }

    private List<MetadataOptionResponse> emailProtocols() {
        return List.of(
                option(EmailProtocol.IMAP, "email_account.protocol.imap", "IMAP", 1, "default")
        );
    }

    private List<MetadataOptionResponse> conversationMessageDirections() {
        return List.of(
                option(ConversationMessageDirection.INBOUND, "conversation.message.direction.inbound", "Entrada", 1, "info"),
                option(ConversationMessageDirection.OUTBOUND, "conversation.message.direction.outbound", "Saida", 2, "success")
        );
    }

    private List<MetadataOptionResponse> conversationMessageTypes() {
        return List.of(
                option(ConversationMessageType.TEXT, "conversation.message.type.text", "Texto", 1, "default"),
                option(ConversationMessageType.TEMPLATE, "conversation.message.type.template", "Template", 2, "primary"),
                option(ConversationMessageType.IMAGE, "conversation.message.type.image", "Imagem", 3, "info"),
                option(ConversationMessageType.AUDIO, "conversation.message.type.audio", "Audio", 4, "secondary"),
                option(ConversationMessageType.DOCUMENT, "conversation.message.type.document", "Documento", 5, "warning")
        );
    }

    private List<MetadataOptionResponse> conversationMessageStatuses() {
        return List.of(
                option(ConversationMessageStatus.RECEIVED, "conversation.message.status.received", "Recebida", 1, "info"),
                option(ConversationMessageStatus.SENT, "conversation.message.status.sent", "Enviada", 2, "success"),
                option(ConversationMessageStatus.DELIVERED, "conversation.message.status.delivered", "Entregue", 3, "success"),
                option(ConversationMessageStatus.READ, "conversation.message.status.read", "Lida", 4, "primary"),
                option(ConversationMessageStatus.FAILED, "conversation.message.status.failed", "Falhou", 5, "error")
        );
    }

    private MetadataOptionResponse option(Enum<?> code, String labelKey, String label, int order, String color) {
        return new MetadataOptionResponse(code.name(), labelKey, label, order, color);
    }
}
