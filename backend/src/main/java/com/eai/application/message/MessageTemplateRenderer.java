package com.eai.application.message;

import java.util.Map;

public final class MessageTemplateRenderer {

    private MessageTemplateRenderer() {
    }

    public static String render(String content, Map<String, String> placeholders) {
        String rendered = content;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return rendered;
    }
}
