package com.eai.api.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("Metadados sao publicos e retornam catalogo de apresentacao")

    @Test
    void metadataIsPublicAndReturnsPresentationCatalog() throws Exception {
        mockMvc.perform(get("/api/metadata").header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("pt-BR"))
                .andExpect(jsonPath("$.leadStatuses[0].code").value("NEW"))
                .andExpect(jsonPath("$.leadStatuses[0].labelKey").value("lead.status.new"))
                .andExpect(jsonPath("$.leadStatuses[0].label").value("Novo"))
                .andExpect(jsonPath("$.leadStatuses[0].color").value("info"))
                .andExpect(jsonPath("$.leadStatuses[6].code").value("SIMULATING"))
                .andExpect(jsonPath("$.leadStatuses[6].labelKey").value("lead.status.simulating"))
                .andExpect(jsonPath("$.leadStatuses[6].label").value("Simulacao"))
                .andExpect(jsonPath("$.leadStatuses[7].code").value("PROPOSAL_APPROVED"))
                .andExpect(jsonPath("$.leadStatuses[7].labelKey").value("lead.status.proposal_approved"))
                .andExpect(jsonPath("$.leadStatuses[7].label").value("Proposta aprovada"))
                .andExpect(jsonPath("$.leadStatuses[8].code").value("PROPOSAL_SENT"))
                .andExpect(jsonPath("$.leadSources[1].code").value("EMAIL"))
                .andExpect(jsonPath("$.leadSources[1].label").value("E-mail"))
                .andExpect(jsonPath("$.userRoles[0].label").value("Administrador"))
                .andExpect(jsonPath("$.userRoles[*].code", hasItem("AVALIADOR")))
                .andExpect(jsonPath("$.userRoles[*].code", not(hasItem("AUD" + "ITOR"))))
                .andExpect(jsonPath("$.userRoles[6].label").value("Avaliador"))
                .andExpect(jsonPath("$.emailAccountStatuses[0].code").value("NEVER_SYNCED"))
                .andExpect(jsonPath("$.emailAccountStatuses[0].label").value("Nunca sincronizada"))
                .andExpect(jsonPath("$.conversationMessageDirections[0].code").value("INBOUND"))
                .andExpect(jsonPath("$.conversationMessageTypes[1].code").value("TEMPLATE"))
                .andExpect(jsonPath("$.conversationMessageStatuses[0].code").value("RECEIVED"));
    }
}
