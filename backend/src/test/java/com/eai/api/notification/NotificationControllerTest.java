package com.eai.api.notification;

import com.eai.application.notification.NotificationRepository;
import com.eai.domain.notification.Notification;
import com.eai.domain.notification.NotificationSeverity;
import com.eai.domain.notification.NotificationType;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class NotificationControllerTest {

    private static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @DisplayName("Usuario lista, conta e marca como lidas apenas as proprias notificacoes")
    @Test
    void userManagesOnlyOwnNotifications() throws Exception {
        String adminToken = login("admin@eai.com", "admin123");
        UUID adminId = UUID.fromString(me(adminToken).get("id").asText());
        UUID sellerId = createUser(adminToken, "Vendedor Notificacao", "vendedor.notificacao@eai.com", "SELLER");

        Notification adminNotification = notificationRepository.save(Notification.create(
                adminId,
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.ERROR,
                "Falha em e-mail",
                "Conta Leads indisponivel",
                "EmailAccount",
                UUID.randomUUID()
        ));
        notificationRepository.save(Notification.create(
                sellerId,
                NotificationType.EMAIL_ACCOUNT_FAILURE,
                NotificationSeverity.ERROR,
                "Falha de outro usuario",
                "Nao deve aparecer",
                "EmailAccount",
                UUID.randomUUID()
        ));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("unreadOnly", "true")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(adminNotification.getId().toString()))
                .andExpect(jsonPath("$[0].read").value(false));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        mockMvc.perform(post("/api/notifications/{id}/read", adminNotification.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt", not(blankOrNullString())));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    private tools.jackson.databind.JsonNode me(String token) throws Exception {
        String response = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private UUID createUser(String token, String name, String email, String role) throws Exception {
        String response = mockMvc.perform(post("/api/users")
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
                                  "storeId": "%s",
                                  "roles": ["%s"]
                                }
                                """.formatted(name, email, role, DEFAULT_COMPANY_ID, DEFAULT_STORE_ID, role)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }
}
