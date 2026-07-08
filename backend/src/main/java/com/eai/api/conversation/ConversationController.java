package com.eai.api.conversation;

import com.eai.application.conversation.ConversationService;
import com.eai.application.security.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<ConversationSummaryResponse> listConversations(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return conversationService.listConversationSummaries(authenticatedUser).stream()
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
}
