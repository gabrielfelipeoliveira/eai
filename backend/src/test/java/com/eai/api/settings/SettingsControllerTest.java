package com.eai.api.settings;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class SettingsControllerTest {

    private static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Admin carrega configuracoes agregadas e atualiza empresa")
    @Test
    void adminCanLoadAggregatedSettingsAndUpdateCompany() throws Exception {
        String token = login("admin@eai.com", "admin123");

        mockMvc.perform(get("/api/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.id").value(DEFAULT_COMPANY_ID.toString()))
                .andExpect(jsonPath("$.store.id").value(DEFAULT_STORE_ID.toString()))
                .andExpect(jsonPath("$.distribution.mode").value("MANUAL"))
                .andExpect(jsonPath("$.availableCompanies[0].id", not(blankOrNullString())))
                .andExpect(jsonPath("$.availableStores[0].id", not(blankOrNullString())))
                .andExpect(jsonPath("$.users.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.templates.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.system.timezone").value("America/Sao_Paulo"));

        mockMvc.perform(put("/api/settings/company")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
{
  "companyId": "%s",
  "name": "Empresa EAI Settings",
  "status": "ACTIVE"
}
""".formatted(DEFAULT_COMPANY_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Empresa EAI Settings"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @DisplayName("Gerente atualiza loja, distribuicao e SLA")
    @Test
    void managerCanUpdateStoreDistributionAndSla() throws Exception {
        String adminToken = login("admin@eai.com", "admin123");
        createUser(adminToken, "Gerente Settings", "gerente.settings@eai.com", "MANAGER");
        String managerToken = login("gerente.settings@eai.com", "manager123");

        mockMvc.perform(put("/api/settings/store")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId": "%s",
                                  "companyId": "%s",
                                  "name": "Loja Settings",
                                  "document": "00000000020191",
                                  "email": "loja.settings@eai.com",
                                  "phone": "1144445555",
                                  "city": "Sao Paulo",
                                  "state": "SP",
                                  "address": "Avenida Settings, 100",
                                  "status": "ACTIVE"
                                }
                                """.formatted(DEFAULT_STORE_ID, DEFAULT_COMPANY_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Loja Settings"))
                .andExpect(jsonPath("$.state").value("SP"));

        mockMvc.perform(put("/api/settings/distribution")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "storeId": "%s",
                                  "mode": "ROUND_ROBIN",
                                  "active": true
                                }
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("ROUND_ROBIN"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(put("/api/settings/sla")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "storeId": "%s",
                                  "minutesToAssign": 10,
                                  "minutesToFirstContact": 25,
                                  "active": true
                                }
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutesToAssign").value(10))
                .andExpect(jsonPath("$.minutesToFirstContact").value(25))
                .andExpect(jsonPath("$.slaActive").value(true));
    }

    @DisplayName("Vendedor nao acessa configuracoes administrativas")
    @Test
    void sellerCannotAccessAdministrativeSettings() throws Exception {
        String adminToken = login("admin@eai.com", "admin123");
        createUser(adminToken, "Vendedor Settings", "vendedor.settings@eai.com", "SELLER");
        String sellerToken = login("vendedor.settings@eai.com", "manager123");

        mockMvc.perform(get("/api/settings")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
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
