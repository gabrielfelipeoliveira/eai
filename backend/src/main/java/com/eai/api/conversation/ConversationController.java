package com.eai.api.conversation;

import com.eai.application.conversation.ConversationFilters;
import com.eai.application.conversation.ConversationService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.whatsapp.WhatsAppTextSenderService;
import com.eai.domain.conversation.ConversationMessageStatus;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@RestController
@RequestMapping("/api/conversations")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
public class ConversationController {

    private final ConversationService conversationService;
    private final WhatsAppTextSenderService whatsAppTextSenderService;

    public ConversationController(ConversationService conversationService, WhatsAppTextSenderService whatsAppTextSenderService) {
        this.conversationService = conversationService;
        this.whatsAppTextSenderService = whatsAppTextSenderService;
    }

    @GetMapping
    public List<ConversationSummaryResponse> listConversations(
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) ConversationMessageStatus messageStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ConversationFilters filters = new ConversationFilters(sellerId, messageStatus, startAt, endAt);
        return conversationService.listConversationSummaries(authenticatedUser, filters).stream()
                .map(ConversationSummaryResponse::fromApplication)
                .toList();
    }

    @GetMapping("/{id}")
    public ConversationResponse getConversation(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ConversationResponse.fromDomain(conversationService.getConversation(id, authenticatedUser));
    }

    @GetMapping("/{id}/messages")
    public List<ConversationMessageResponse> listMessages(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return conversationService.listMessages(id, authenticatedUser).stream()
                .map(ConversationMessageResponse::fromDomain)
                .toList();
    }

    @PostMapping("/{id}/messages")
    public ConversationMessageResponse sendTextMessage(
            @PathVariable UUID id,
            @Valid @RequestBody ConversationTextMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ConversationMessageResponse.fromTextSendResult(whatsAppTextSenderService.sendText(id, request.content(), authenticatedUser));
    }
}
