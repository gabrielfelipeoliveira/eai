package com.eai.api.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eai.whatsapp.cloud-api.verify-token=test-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void verifiesWebhookWhenTokenMatches() throws Exception {
        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-token")
                        .param("hub.challenge", "challenge-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-123"));
    }

    @Test
    void rejectsWebhookVerificationWhenTokenDoesNotMatch() throws Exception {
        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "challenge-123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void receivesWebhookEventsWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "object": "whatsapp_business_account",
                                  "entry": []
                                }
                                """))
                .andExpect(status().isAccepted());
    }
}
