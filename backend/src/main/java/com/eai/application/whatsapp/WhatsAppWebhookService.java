package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookService.class);

    private final WhatsAppChannelSettings settings;

    public WhatsAppWebhookService(WhatsAppChannelSettings settings) {
        this.settings = settings;
    }

    public String verifyWebhook(String mode, String verifyToken, String challenge) {
        if (!settings.webhookConfigured()) {
            throw new ApplicationException("WHATSAPP_WEBHOOK_NOT_CONFIGURED", "WhatsApp webhook verification token is not configured");
        }
        if (!"subscribe".equals(mode) || !settings.verifyToken().equals(verifyToken)) {
            throw new ForbiddenException("Invalid WhatsApp webhook verification request");
        }
        logger.info("WhatsApp webhook verified successfully");
        return challenge;
    }

    public void receiveEvent(String payload) {
        logger.info("WhatsApp webhook event received: {}", payload);
    }
}
