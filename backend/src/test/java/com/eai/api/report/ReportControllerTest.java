package com.eai.api.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    private static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Relatorios e exportacao CSV funcionam")

    @Test
    void reportsAndCsvExportWork() throws Exception {
        String token = login();
        String soldLeadId = createLead(token, "Cliente Venda Reports", "11999991001", "WEBSITE", "32000");
        String lostLeadId = createLead(token, "Cliente Perda Reports", "11999991002", "FACEBOOK", null);

        mockMvc.perform(patch("/api/leads/{id}/assign-to-me", soldLeadId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/leads/{id}/assign-to-me", lostLeadId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/leads/{id}/status", soldLeadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SOLD\",\"description\":\"Venda realizada\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/leads/{id}/status", lostLeadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOST\",\"description\":\"Lead perdido\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/leads")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leadCount", greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/reports/sellers")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId", not(blankOrNullString())))
                .andExpect(jsonPath("$[0].soldLeads", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/reports/sources")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source", not(blankOrNullString())));

        mockMvc.perform(get("/api/reports/lost")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leadId", not(blankOrNullString())));

        mockMvc.perform(get("/api/reports/sales")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saleValue").value(32000));

        mockMvc.perform(get("/api/reports/sla")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadCount", greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/reports/leads/export.csv")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("reports-leads.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("period,leadCount,soldLeads,lostLeads,conversionRate")));

        mockMvc.perform(get("/api/reports/sellers/export.csv")
                        .header("Authorization", "Bearer " + token)
                        .param("storeId", DEFAULT_STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("reports-sellers.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("sellerId,sellerName,leadCount,soldLeads,lostLeads")));
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

    private String createLead(String token, String customerName, String customerPhone, String source, String saleValue) throws Exception {
        String saleValueJson = saleValue == null ? "" : "\"saleValue\": %s,".formatted(saleValue);
        String response = mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "%s",
                                  "storeId": "%s",
                                  "customerName": "%s",
                                  "customerPhone": "%s",
                                  "customerCity": "Sao Paulo",
                                  "vehicleInterest": "Honda Civic",
                                  "source": "%s",
                                  %s
                                  "lostReason": "Sem retorno",
                                  "originalMessage": "Lead para relatorio"
                                }
                                """.formatted(DEFAULT_COMPANY_ID, DEFAULT_STORE_ID, customerName, customerPhone, source, saleValueJson)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }
}
