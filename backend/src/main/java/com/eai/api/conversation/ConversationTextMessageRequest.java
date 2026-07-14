package com.eai.api.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConversationTextMessageRequest(
        @NotBlank
        @Size(max = 4096)
        String content
) {
}
