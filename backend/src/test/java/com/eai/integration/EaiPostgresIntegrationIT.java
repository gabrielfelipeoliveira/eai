package com.eai.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EaiPostgresIntegrationIT extends AbstractPostgresIntegrationTest {

    private static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("Flyway executa migrations e seeds em PostgreSQL real")
    @Test
    void flywayRunsMigrationsAndSeedsOnPostgres() {
        Integer flywayRows = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where success = true", Integer.class);
        Integer users = jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
        Integer companies = jdbcTemplate.queryForObject("select count(*) from companies", Integer.class);

        assertNotNull(flywayRows);
        assertNotNull(users);
        assertNotNull(companies);
        assertTrue(flywayRows >= 9);
        assertTrue(users >= 7);
        assertTrue(companies >= 1);
    }

    @DisplayName("Autenticacao, tenancy, leads e conversas funcionam com PostgreSQL real")
    @Test
    void criticalHttpFlowsWorkOnPostgres() throws Exception {
        String token = login();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@eai.com"))
                .andExpect(jsonPath("$.companyId").value(DEFAULT_COMPANY_ID.toString()))
                .andExpect(jsonPath("$.storeId").value(DEFAULT_STORE_ID.toString()))
                .andExpect(jsonPath("$.roles[*]", hasItem("ADMIN")));

        String leadId = createLead(token);

        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .param("phone", "11999880016"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].id", hasItem(leadId)));

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"object":"whatsapp_business_account","entry":[{"changes":[{"value":{"contacts":[{"profile":{"name":"Cliente Postgres"},"wa_id":"5511999880016"}],"messages":[{"from":"5511999880016","id":"wamid.eai016-postgres","timestamp":"1710000000","type":"text","text":{"body":"Quero falar sobre o Civic"}}]}}]}]}
                                """))
                .andExpect(status().isAccepted());

        String conversationsResponse = mockMvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode conversations = objectMapper.readTree(conversationsResponse);
        String conversationId = findConversationId(conversations, leadId);
        assertNotNull(conversationId);

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direction").value("INBOUND"))
                .andExpect(jsonPath("$[0].type").value("TEXT"))
                .andExpect(jsonPath("$[0].status").value("READ"))
                .andExpect(jsonPath("$[0].externalMessageId").value("wamid.eai016-postgres"))
                .andExpect(jsonPath("$[0].content").value("Quero falar sobre o Civic"));
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@eai.com","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createLead(String token) throws Exception {
        String response = mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyId":"%s","storeId":"%s","customerName":"Cliente Postgres EAI016","customerPhone":"11999880016","customerEmail":"postgres.eai016@eai.com","customerCity":"Sao Paulo","vehicleInterest":"Honda Civic","source":"MANUAL","originalMessage":"Lead criado pelo teste de integracao PostgreSQL"}
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.customerPhone").value("+5511999880016"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String findConversationId(JsonNode conversations, String leadId) {
        for (JsonNode conversation : conversations) {
            if (leadId.equals(conversation.path("leadId").asText())) {
                return conversation.path("id").asText();
            }
        }
        return null;
    }
}
