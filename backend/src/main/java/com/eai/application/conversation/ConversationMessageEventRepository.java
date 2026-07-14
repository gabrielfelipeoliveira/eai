package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationMessageEvent;

public interface ConversationMessageEventRepository {

    ConversationMessageEvent save(ConversationMessageEvent event);
}
