package com.eai.application.whatsapp;

public interface WhatsAppChannelSettings {

    String phoneNumberId();

    String accessToken();

    String appSecret();

    String graphApiVersion();

    String verifyToken();

    String companyId();

    String storeId();

    boolean webhookConfigured();

    boolean inboundPersistenceConfigured();

    boolean templateSendingConfigured();
}
