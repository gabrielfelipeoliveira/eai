package com.eai.application.whatsapp;

public interface WhatsAppTextClient {

    WhatsAppTextProviderResult sendText(String phone, String content);
}
