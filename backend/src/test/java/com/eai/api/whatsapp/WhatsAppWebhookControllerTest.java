package com.eai.api.whatsapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eai.whatsapp.cloud-api.verify-token=test-token",
        "eai.whatsapp.cloud-api.app-secret=test-secret"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class WhatsAppWebhookControllerTest {

    private static final String APP_SECRET = "test-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Webhook do WhatsApp valida quando o token confere")
    @Test
    void verifiesWebhookWhenTokenMatches() throws Exception {
        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-token")
                        .param("hub.challenge", "challenge-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-123"));
    }

    @DisplayName("Webhook do WhatsApp rejeita token divergente")
    @Test
    void rejectsWebhookWhenTokenDoesNotMatch() throws Exception {
        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "challenge-123"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Webhook do WhatsApp aceita payload com assinatura valida")
    @Test
    void receivesWebhookEventWhenSignatureMatches() throws Exception {
        String payload = """
                {"object":"whatsapp_business_account","entry":[]}
                """;

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .header("X-Hub-Signature-256", signatureFor(payload))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted());
    }

    @DisplayName("Webhook do WhatsApp rejeita payload sem assinatura")
    @Test
    void rejectsWebhookEventWithoutSignature() throws Exception {
        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"object":"whatsapp_business_account","entry":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Webhook do WhatsApp rejeita payload com assinatura invalida")
    @Test
    void rejectsWebhookEventWithInvalidSignature() throws Exception {
        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .header("X-Hub-Signature-256", "sha256=0000000000000000000000000000000000000000000000000000000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"object":"whatsapp_business_account","entry":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Webhook do WhatsApp rejeita payload com assinatura malformada")
    @Test
    void rejectsWebhookEventWithMalformedSignature() throws Exception {
        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .header("X-Hub-Signature-256", "sha256=not-hex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"object":"whatsapp_business_account","entry":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Webhook persiste mensagem recebida e cria conversa por telefone")
    @Test
    void persistsIncomingMessageAndCreatesConversationByPhone() throws Exception {
        String payload = """
                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"contacts":[{"profile":{"name":"Mariana Alves"},"wa_id":"5511988880001"}],"messages":[{"from":"5511988880001","id":"wamid.test-inbound-001","timestamp":"1710000000","type":"text","text":{"body":"Tenho interesse no Civic"}}]}}]}]}
                """;

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .header("X-Hub-Signature-256", signatureFor(payload))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
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
                .andExpect(jsonPath("$[0].status").value("READ"))
                .andExpect(jsonPath("$[0].externalMessageId").value("wamid.test-inbound-001"))
                .andExpect(jsonPath("$[0].content").value("Tenho interesse no Civic"));

        mockMvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unreadCount").value(0));
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

    private String signatureFor(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
