package com.eai.api.lead;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeadControllerTest {

    private static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID DEFAULT_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FIRST_CONTACT_TEMPLATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leadManagementFlowWorks() throws Exception {
        String token = login();

        String leadId = createManualLead(token);

        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .param("phone", "11999990000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].id", not(blankOrNullString())));

        mockMvc.perform(patch("/api/leads/{id}/assign-to-me", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedToUserId").value(DEFAULT_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(patch("/api/leads/{id}/status", leadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "FIRST_CONTACT",
                                  "description": "Primeiro contato realizado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FIRST_CONTACT"));

        mockMvc.perform(patch("/api/leads/{id}/status", leadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SIMULATING",
                                  "description": "Simulacao enviada para F&I"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SIMULATING"))
                .andExpect(jsonPath("$.lastContactAt", not(blankOrNullString())));

        mockMvc.perform(patch("/api/leads/{id}/status", leadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROPOSAL_APPROVED",
                                  "description": "Proposta aprovada pela financeira"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSAL_APPROVED"))
                .andExpect(jsonPath("$.lastContactAt", not(blankOrNullString())));

        mockMvc.perform(post("/api/leads/{id}/notes", leadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "Cliente pediu proposta por WhatsApp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Cliente pediu proposta por WhatsApp"));

        mockMvc.perform(get("/api/leads/{id}/notes", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value("Cliente pediu proposta por WhatsApp"));

        mockMvc.perform(get("/api/leads/{id}/history", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].newStatus").value("PROPOSAL_APPROVED"))
                .andExpect(jsonPath("$[0].description").value("Proposta aprovada pela financeira"))
                .andExpect(jsonPath("$[1].newStatus").value("SIMULATING"))
                .andExpect(jsonPath("$[2].newStatus").value("FIRST_CONTACT"));

        mockMvc.perform(get("/api/templates/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Primeiro contato"));

        mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "storeId": "%s",
                                  "name": "Follow up",
                                  "type": "FOLLOW_UP",
                                  "content": "Ola {cliente}, posso ajudar com o {veiculo}?",
                                  "active": true
                                }
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Follow up"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(post("/api/leads/{id}/whatsapp-link", leadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": "%s"
                                }
                                """.formatted(FIRST_CONTACT_TEMPLATE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", containsString("https://wa.me/5511999990000?text=")))
                .andExpect(jsonPath("$.message", containsString("Cliente Teste Lead")))
                .andExpect(jsonPath("$.message", containsString("Honda Civic")))
                .andExpect(jsonPath("$.communicationId", not(blankOrNullString())));

        mockMvc.perform(get("/api/leads/{id}/communications", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].channel").value("WHATSAPP_LINK"))
                .andExpect(jsonPath("$[0].templateId").value(FIRST_CONTACT_TEMPLATE_ID.toString()))
                .andExpect(jsonPath("$[0].message", containsString("Cliente Teste Lead")));

        mockMvc.perform(get("/api/leads/{id}/conversation-messages", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direction").value("OUTBOUND"))
                .andExpect(jsonPath("$[0].type").value("TEMPLATE"))
                .andExpect(jsonPath("$[0].status").value("SENT"))
                .andExpect(jsonPath("$[0].content", containsString("Cliente Teste Lead")));

        String availableLeadId = createManualLead(token, "Cliente Pipeline Novo", "11999990001");
        String assignedLeadId = createManualLead(token, "Cliente Pipeline Atribuido", "11999990002");
        mockMvc.perform(patch("/api/leads/{id}/assign-to-me", assignedLeadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(get("/api/pipeline")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SIMULATING").isArray())
                .andExpect(jsonPath("$.PROPOSAL_APPROVED[0].id").value(leadId))
                .andExpect(jsonPath("$.AVAILABLE[0].id").value(availableLeadId))
                .andExpect(jsonPath("$.ASSIGNED[0].id").value(assignedLeadId));

        String followUpId = createFollowUp(token, leadId);

        mockMvc.perform(get("/api/leads/{id}/follow-ups", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Retornar proposta"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mockMvc.perform(get("/api/follow-ups/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", not(blankOrNullString())));

        mockMvc.perform(patch("/api/follow-ups/{id}/complete", followUpId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.completedAt", not(blankOrNullString())));

        mockMvc.perform(get("/api/leads/{id}/history", leadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Follow-up completed: Retornar proposta"));
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@eai.com",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createManualLead(String token) throws Exception {
        return createManualLead(token, "Cliente Teste Lead", "11999990000");
    }

    private String createManualLead(String token, String customerName, String customerPhone) throws Exception {
        String response = mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "storeId": "%s",
                                  "customerName": "%s",
                                  "customerPhone": "%s",
                                  "customerEmail": "lead.teste@eai.com",
                                  "customerCity": "Sao Paulo",
                                  "vehicleInterest": "Honda Civic",
                                  "source": "MANUAL",
                                  "originalMessage": "Lead criado pelo teste"
                                }
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID, customerName, customerPhone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asText();
    }

    private String createFollowUp(String token, String leadId) throws Exception {
        String dueAt = Instant.now().plus(1, ChronoUnit.DAYS).toString();
        String response = mockMvc.perform(post("/api/leads/{id}/follow-ups", leadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Retornar proposta",
                                  "description": "Enviar simulacao atualizada",
                                  "dueAt": "%s"
                                }
                                """.formatted(dueAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Retornar proposta"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }
}
