package com.eai.api.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Login do admin seed retorna tokens")
    @Test
    void loginReturnsTokensForSeedAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@eai.com",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @DisplayName("Login do avaliador seed retorna token e papel")
    @Test
    void loginReturnsTokenForSeedAvaliador() throws Exception {
        String token = login("avaliador@eai.com");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("avaliador@eai.com"))
                .andExpect(jsonPath("$.roles[*]", hasItem("AVALIADOR")));
    }

    @DisplayName("Avaliador nao acessa endpoints legados de auditoria")
    @Test
    void avaliadorCannotAccessAuditorOnlyLegacyEndpoints() throws Exception {
        String token = login("avaliador@eai.com");

        mockMvc.perform(get("/api/reports/leads")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Endpoint de usuarios exige token de acesso")
    @Test
    void usersEndpointRequiresAccessToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Segundo login invalida refresh token anterior")
    @Test
    void secondLoginInvalidatesRefreshTokenIssuedByFirstLogin() throws Exception {
        JsonNode firstLogin = loginTokens("admin@eai.com");
        loginTokens("admin@eai.com");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(firstLogin.get("refreshToken").asText())))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Usuario desativado nao usa access token nem refresh token")
    @Test
    @DirtiesContext
    void deactivatedUserCannotUseAccessOrRefreshToken() throws Exception {
        JsonNode avaliadorLogin = loginTokens("avaliador@eai.com");
        String adminAccessToken = login("admin@eai.com");

        mockMvc.perform(patch("/api/users/00000000-0000-0000-0000-000000000031/deactivate")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + avaliadorLogin.get("accessToken").asText()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(avaliadorLogin.get("refreshToken").asText())))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Login aceita preflight CORS do frontend")
    @Test
    void loginAllowsFrontendCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private String login(String email) throws Exception {
        return loginTokens(email).get("accessToken").asText();
    }

    private JsonNode loginTokens(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "admin123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }
}
