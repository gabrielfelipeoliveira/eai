package com.eai.api.lead;

import com.eai.application.distribution.LeadDistributionService;
import com.eai.application.conversation.ConversationService;
import com.eai.application.lead.CreateLeadCommand;
import com.eai.application.lead.LeadItemCommand;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.lead.LeadService;
import com.eai.application.lead.LeadVehicleCommand;
import com.eai.application.lead.UpdateLeadCommand;
import com.eai.application.message.MessageTemplateService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.whatsapp.SendWhatsAppTemplateCommand;
import com.eai.application.whatsapp.WhatsAppTemplateSenderService;
import com.eai.api.conversation.ConversationMessageResponse;
import com.eai.api.message.LeadCommunicationResponse;
import com.eai.api.message.WhatsAppTemplateSendRequest;
import com.eai.api.message.WhatsAppTemplateSendResponse;
import com.eai.api.message.WhatsappLinkRequest;
import com.eai.api.message.WhatsappLinkResponse;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
public class LeadController {

    private final LeadService leadService;
    private final MessageTemplateService templateService;
    private final WhatsAppTemplateSenderService whatsAppTemplateSenderService;
    private final LeadDistributionService distributionService;
    private final ConversationService conversationService;

    public LeadController(
            LeadService leadService,
            MessageTemplateService templateService,
            WhatsAppTemplateSenderService whatsAppTemplateSenderService,
            LeadDistributionService distributionService,
            ConversationService conversationService
    ) {
        this.leadService = leadService;
        this.templateService = templateService;
        this.whatsAppTemplateSenderService = whatsAppTemplateSenderService;
        this.distributionService = distributionService;
        this.conversationService = conversationService;
    }

    @PostMapping
    public LeadResponse createLead(@Valid @RequestBody LeadRequest request, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(leadService.createLead(new CreateLeadCommand(
                request.companyId(),
                request.storeId(),
                request.customerName(),
                request.customerPhone(),
                request.additionalPhones(),
                request.customerEmail(),
                request.customerCity(),
                request.vehicleInterest(),
                request.source(),
                request.originalMessage(),
                request.assignedToUserId(),
                request.lostReason(),
                request.saleValue(),
                request.saleCurrency(),
                toItemCommand(request.item())
        ), authenticatedUser));
    }

    @GetMapping
    public PageResponse<LeadResponse> listLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) UUID assignedToUserId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String vehicle,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return PageResponse.from(leadService.listLeads(new LeadSearchCriteria(
                status,
                source,
                assignedToUserId,
                storeId,
                createdFrom,
                createdTo,
                text,
                vehicle,
                phone,
                null,
                null
        ), page, size, authenticatedUser), this::toResponse);
    }

    @GetMapping("/{id}")
    public LeadResponse getLead(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(leadService.getLead(id, authenticatedUser));
    }

    @PutMapping("/{id}")
    public LeadResponse updateLead(@PathVariable UUID id, @Valid @RequestBody LeadRequest request, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(leadService.updateLead(id, new UpdateLeadCommand(
                request.companyId(),
                request.storeId(),
                request.customerName(),
                request.customerPhone(),
                request.additionalPhones(),
                request.customerEmail(),
                request.customerCity(),
                request.vehicleInterest(),
                request.source(),
                request.originalMessage(),
                request.status() == null ? LeadStatus.AVAILABLE : request.status(),
                request.assignedToUserId(),
                request.firstContactAt(),
                request.lastContactAt(),
                request.lostReason(),
                request.saleValue(),
                request.saleCurrency(),
                toItemCommand(request.item())
        ), authenticatedUser));
    }

    @PatchMapping("/{id}/status")
    public LeadResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody LeadStatusRequest request, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(leadService.changeStatus(id, request.status(), request.description(), authenticatedUser));
    }

    @PatchMapping("/{id}/assign-to-me")
    public LeadResponse assignToMe(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(leadService.assignToMe(id, authenticatedUser));
    }

    @PatchMapping("/{id}/assign/{userId}")
    public LeadResponse assign(@PathVariable UUID id, @PathVariable UUID userId, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(leadService.assign(id, userId, authenticatedUser));
    }

    @PostMapping("/{id}/assign-automatically")
    public LeadResponse assignAutomatically(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return toResponse(distributionService.assignAutomatically(id, authenticatedUser));
    }

    @PostMapping("/distribute-pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<LeadResponse> distributePending(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return distributionService.distributePending(authenticatedUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/sla/overdue")
    public List<LeadResponse> listOverdue(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return distributionService.listOverdue(authenticatedUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{id}/notes")
    public LeadNoteResponse addNote(@PathVariable UUID id, @Valid @RequestBody LeadNoteRequest request, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return LeadNoteResponse.fromDomain(leadService.addNote(id, request.note(), authenticatedUser));
    }

    @GetMapping("/{id}/history")
    public List<LeadHistoryResponse> listHistory(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return leadService.listHistory(id, authenticatedUser).stream()
                .map(LeadHistoryResponse::fromDomain)
                .toList();
    }

    @PostMapping("/{id}/whatsapp-link")
    public WhatsappLinkResponse generateWhatsappLink(
            @PathVariable UUID id,
            @Valid @RequestBody WhatsappLinkRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return WhatsappLinkResponse.fromResult(templateService.generateWhatsappLink(id, request.templateId(), authenticatedUser));
    }

    @PostMapping("/{id}/whatsapp-template")
    public WhatsAppTemplateSendResponse sendWhatsAppTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody WhatsAppTemplateSendRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return WhatsAppTemplateSendResponse.fromResult(whatsAppTemplateSenderService.sendTemplate(
                id,
                new SendWhatsAppTemplateCommand(request.templateId(), request.languageCode()),
                authenticatedUser
        ));
    }

    @GetMapping("/{id}/communications")
    public List<LeadCommunicationResponse> listCommunications(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return templateService.listLeadCommunications(id, authenticatedUser).stream()
                .map(LeadCommunicationResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}/conversation-messages")
    public List<ConversationMessageResponse> listConversationMessages(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return conversationService.listLeadMessages(id, authenticatedUser).stream()
                .map(ConversationMessageResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}/notes")
    public List<LeadNoteResponse> listNotes(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return leadService.listNotes(id, authenticatedUser).stream()
                .map(LeadNoteResponse::fromDomain)
                .toList();
    }

    @PostMapping("/{id}/tags")
    public LeadTagResponse addTag(@PathVariable UUID id, @Valid @RequestBody LeadTagRequest request, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return LeadTagResponse.fromDomain(leadService.addTag(id, request.name(), authenticatedUser));
    }

    @GetMapping("/{id}/tags")
    public List<LeadTagResponse> listTags(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return leadService.listTags(id, authenticatedUser).stream()
                .map(LeadTagResponse::fromDomain)
                .toList();
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public void deleteTag(@PathVariable UUID id, @PathVariable UUID tagId, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        leadService.deleteTag(id, tagId, authenticatedUser);
    }

    private LeadResponse toResponse(Lead lead) {
        LeadSlaPolicy policy = distributionService.findOrDefaultSla(lead.getCompanyId(), lead.getStoreId());
        if (!policy.isActive()) {
            return LeadResponse.fromDomain(lead);
        }
        return LeadResponse.fromDomain(lead, policy.getMinutesToAssign(), policy.getMinutesToFirstContact(), Instant.now());
    }

    private LeadItemCommand toItemCommand(LeadRequest.LeadItemRequest request) {
        return request == null ? null : new LeadItemCommand(request.name(), toVehicleCommand(request.vehicle()));
    }

    private LeadVehicleCommand toVehicleCommand(LeadRequest.LeadVehicleRequest request) {
        return request == null ? null : new LeadVehicleCommand(request.name(), request.year(), request.model(), request.value());
    }
}
