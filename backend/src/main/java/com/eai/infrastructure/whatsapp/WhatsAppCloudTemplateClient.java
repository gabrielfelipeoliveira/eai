package com.eai.infrastructure.whatsapp;

import com.eai.application.whatsapp.WhatsAppTemplateClient;
import com.eai.application.whatsapp.WhatsAppTemplateProviderResult;
import com.eai.infrastructure.config.WhatsAppCloudApiProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WhatsAppCloudTemplateClient implements WhatsAppTemplateClient {

    private static final String GRAPH_BASE_URL = "https://graph.facebook.com";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final WhatsAppCloudApiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WhatsAppCloudTemplateClient(WhatsAppCloudApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    @Override
    public WhatsAppTemplateProviderResult sendTemplate(String phone, String templateName, String languageCode, List<String> bodyParameters) {
        try {
            String requestBody = objectMapper.writeValueAsString(requestBody(phone, templateName, languageCode, bodyParameters));
            HttpRequest request = HttpRequest.newBuilder(endpoint())
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            boolean successful = response.statusCode() >= 200 && response.statusCode() < 300;
            return new WhatsAppTemplateProviderResult(
                    successful,
                    response.statusCode(),
                    successful ? externalMessageId(response.body()) : null,
                    response.body()
            );
        } catch (Exception exception) {
            return new WhatsAppTemplateProviderResult(false, 0, null, exception.getMessage());
        }
    }

    private URI endpoint() {
        return URI.create(GRAPH_BASE_URL + "/" + graphApiVersion() + "/" + properties.phoneNumberId() + "/messages");
    }

    private String graphApiVersion() {
        if (properties.graphApiVersion() == null || properties.graphApiVersion().isBlank()) {
            return "v25.0";
        }
        return properties.graphApiVersion().trim();
    }

    private Map<String, Object> requestBody(String phone, String templateName, String languageCode, List<String> bodyParameters) {
        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode));
        if (bodyParameters != null && !bodyParameters.isEmpty()) {
            template.put("components", List.of(Map.of(
                    "type", "body",
                    "parameters", bodyParameters.stream()
                            .map(value -> Map.of("type", "text", "text", value))
                            .toList()
            )));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", phone);
        body.put("type", "template");
        body.put("template", template);
        return body;
    }

    private String externalMessageId(String responseBody) {
        try {
            JsonNode messages = objectMapper.readTree(responseBody).path("messages");
            if (messages.isArray() && !messages.isEmpty()) {
                String id = messages.get(0).path("id").asText();
                return id == null || id.isBlank() ? null : id;
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }
}
