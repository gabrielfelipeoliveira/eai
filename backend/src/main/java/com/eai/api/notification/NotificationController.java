package com.eai.api.notification;

import com.eai.application.notification.NotificationService;
import com.eai.application.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return notificationService.listMine(unreadOnly, limit, authenticatedUser).stream()
                .map(NotificationResponse::fromDomain)
                .toList();
    }

    @GetMapping("/unread-count")
    public UnreadNotificationCountResponse unreadCount(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return new UnreadNotificationCountResponse(notificationService.countUnreadMine(authenticatedUser));
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return NotificationResponse.fromDomain(notificationService.markRead(id, authenticatedUser));
    }

    @PostMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        notificationService.markAllRead(authenticatedUser);
    }
}
