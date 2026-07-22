package com.eai.api.conversation;

import com.eai.application.conversation.ConversationFilters;
import com.eai.application.conversation.ConversationMediaDownload;
import com.eai.application.conversation.ConversationMediaService;
import com.eai.application.conversation.ConversationService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.whatsapp.WhatsAppMediaSenderService;
import com.eai.application.whatsapp.WhatsAppTextSenderService;
import com.eai.domain.conversation.ConversationMessageStatus;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@RestController
@RequestMapping("/api/conversations")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER')")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final WhatsAppTextSenderService whatsAppTextSenderService;
    private final WhatsAppMediaSenderService whatsAppMediaSenderService;
    private final ConversationMediaService conversationMediaService;

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

    @PostMapping(path = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ConversationMessageResponse sendMediaMessage(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "caption", required = false) String caption,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) throws IOException {
        return ConversationMessageResponse.fromMediaSendResult(whatsAppMediaSenderService.sendMedia(
                id,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                caption,
                authenticatedUser
        ));
    }

    @GetMapping("/{conversationId}/messages/{messageId}/media")
    public ResponseEntity<byte[]> downloadMedia(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ConversationMediaDownload media = conversationMediaService.download(conversationId, messageId, authenticatedUser);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.mimeType()))
                .contentLength(media.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(media.fileName()).build().toString())
                .body(media.content());
    }
}
