package com.eai.application.whatsapp;

public interface WhatsAppChannelSettings {

    String verifyToken();

    String companyId();

    String storeId();

    boolean webhookConfigured();

    boolean inboundPersistenceConfigured();
}
