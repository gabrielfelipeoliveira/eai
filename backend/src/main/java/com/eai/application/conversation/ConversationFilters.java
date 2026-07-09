package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationMessageStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationFilters(
        UUID sellerId,
        ConversationMessageStatus messageStatus,
        Instant startAt,
        Instant endAt
) {
}
