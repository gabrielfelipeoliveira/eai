package com.eai.api.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eai.whatsapp.cloud-api.verify-token=test-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void persistsIncomingMessageAndCreatesConversationByPhone() throws Exception {
        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "object": "whatsapp_business_account",
                                  "entry": [
                                    {
                                      "changes": [
                                        {
                                          "value": {
                                            "contacts": [
                                              {
                                                "profile": { "name": "Mariana Alves" },
                                                "wa_id": "5511988880001"
                                              }
                                            ],
                                            "messages": [
                                              {
                                                "from": "5511988880001",
                                                "id": "wamid.test-inbound-001",
                                                "timestamp": "1710000000",
                                                "type": "text",
                                                "text": { "body": "Tenho interesse no Civic" }
                                              }
                                            ]
                                          }
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isAccepted());

        String token = login();
        String conversationsResponse = mockMvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode conversations = objectMapper.readTree(conversationsResponse);
        String conversationId = null;
        for (JsonNode conversation : conversations) {
            if ("00000000-0000-0000-0000-000000000501".equals(conversation.path("leadId").asText())) {
                conversationId = conversation.get("id").asText();
                break;
            }
        }
        assertNotNull(conversationId);

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direction").value("INBOUND"))
                .andExpect(jsonPath("$[0].type").value("TEXT"))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$[0].externalMessageId").value("wamid.test-inbound-001"))
                .andExpect(jsonPath("$[0].content").value("Tenho interesse no Civic"));
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@eai.com","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
