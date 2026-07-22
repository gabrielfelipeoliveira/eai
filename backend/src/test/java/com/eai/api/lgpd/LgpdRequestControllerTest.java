package com.eai.api.lgpd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class LgpdRequestControllerTest {

    private static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Admin cria, lista, detalha e registra tratamento LGPD")
    @Test
    void adminManagesLgpdRequestFlow() throws Exception {
        String token = login("admin@eai.com", "admin123");

        String createResponse = mockMvc.perform(post("/api/lgpd-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "storeId": "%s",
                                  "dataSubjectName": "Cliente LGPD",
                                  "dataSubjectPhone": "+5511999990000",
                                  "dataSubjectEmail": "cliente.lgpd@eai.com",
                                  "requestType": "ACCESS",
                                  "description": "Titular solicitou acesso aos dados"
                                }
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.actions.length()").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String requestId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/lgpd-requests")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "OPEN")
                        .param("companyId", DEFAULT_COMPANY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/lgpd-requests/{id}", requestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.requestType").value("ACCESS"));

        mockMvc.perform(post("/api/lgpd-requests/{id}/actions", requestId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actionType": "ACCESS",
                                  "resolution": "Relatorio entregue manualmente ao titular",
                                  "finalStatus": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt", not(blankOrNullString())))
                .andExpect(jsonPath("$.actions[0].actionType").value("ACCESS"))
                .andExpect(jsonPath("$.actions[0].finalStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.actions[0].executorUserId", not(blankOrNullString())));
    }

    @DisplayName("Gerente e vendedor recebem 403 em solicitacoes LGPD")
    @Test
    void nonAdminsReceiveForbidden() throws Exception {
        String adminToken = login("admin@eai.com", "admin123");
        createUser(adminToken, "Gerente LGPD", "gerente.lgpd@eai.com", "MANAGER");
        createUser(adminToken, "Vendedor LGPD", "vendedor.lgpd@eai.com", "SELLER");

        mockMvc.perform(get("/api/lgpd-requests")
                        .header("Authorization", "Bearer " + login("gerente.lgpd@eai.com", "manager123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/lgpd-requests")
                        .header("Authorization", "Bearer " + login("vendedor.lgpd@eai.com", "manager123")))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Payload invalido de LGPD retorna 400")
    @Test
    void invalidPayloadReturnsBadRequest() throws Exception {
        String token = login("admin@eai.com", "admin123");

        mockMvc.perform(post("/api/lgpd-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "requestType": "ACCESS",
                                  "description": "Sem titular"
                                }
                                """.formatted(DEFAULT_COMPANY_ID)))
                .andExpect(status().isBadRequest());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private void createUser(String token, String name, String email, String role) throws Exception {
        String storeId = "MANAGER".equals(role) ? null : "\"" + DEFAULT_STORE_ID + "\"";
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "manager123",
                                  "phone": "11900000000",
                                  "jobTitle": "%s",
                                  "companyId": "%s",
                                  "storeId": %s,
                                  "roles": ["%s"]
                                }
                                """.formatted(name, email, role, DEFAULT_COMPANY_ID, storeId, role)))
                .andExpect(status().isOk());
    }
}
