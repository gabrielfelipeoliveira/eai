package com.eai.infrastructure.whatsapp;

import com.eai.application.whatsapp.WhatsAppTemplateClient;
import com.eai.application.whatsapp.WhatsAppTemplateProviderResult;
import com.eai.application.whatsapp.WhatsAppMediaClient;
import com.eai.application.whatsapp.WhatsAppMediaDownload;
import com.eai.application.whatsapp.WhatsAppMediaMetadata;
import com.eai.application.whatsapp.WhatsAppMediaSendResult;
import com.eai.application.whatsapp.WhatsAppMediaUploadResult;
import com.eai.application.whatsapp.WhatsAppOutboundMediaType;
import com.eai.application.whatsapp.WhatsAppTextClient;
import com.eai.application.whatsapp.WhatsAppTextProviderResult;
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
import java.util.UUID;

@Component
public class WhatsAppCloudTemplateClient implements WhatsAppTemplateClient, WhatsAppTextClient, WhatsAppMediaClient {

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
            HttpResponse<String> response = post(requestBody(phone, templateName, languageCode, bodyParameters));
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

    @Override
    public WhatsAppTextProviderResult sendText(String phone, String content) {
        try {
            HttpResponse<String> response = post(textRequestBody(phone, content));
            boolean successful = response.statusCode() >= 200 && response.statusCode() < 300;
            return new WhatsAppTextProviderResult(
                    successful,
                    response.statusCode(),
                    successful ? externalMessageId(response.body()) : null,
                    response.body()
            );
        } catch (Exception exception) {
            return new WhatsAppTextProviderResult(false, 0, null, exception.getMessage());
        }
    }

    @Override
    public WhatsAppMediaMetadata fetchMediaMetadata(String mediaId) {
        try {
            HttpRequest request = HttpRequest.newBuilder(mediaEndpoint(mediaId))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(response.body());
            }
            JsonNode body = objectMapper.readTree(response.body());
            return new WhatsAppMediaMetadata(
                    text(body.path("id")),
                    text(body.path("url")),
                    text(body.path("mime_type")),
                    body.path("file_size").isNumber() ? body.path("file_size").asLong() : null,
                    text(body.path("sha256")),
                    response.body()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not fetch WhatsApp media metadata", exception);
        }
    }

    @Override
    public WhatsAppMediaDownload downloadMedia(WhatsAppMediaMetadata metadata) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(metadata.url()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            return new WhatsAppMediaDownload(response.body(), metadata.rawResponse());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not download WhatsApp media", exception);
        }
    }

    @Override
    public WhatsAppMediaUploadResult uploadMedia(String fileName, String mimeType, byte[] content) {
        try {
            String boundary = "----eai-" + UUID.randomUUID();
            HttpRequest request = HttpRequest.newBuilder(mediaUploadEndpoint())
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody(boundary, fileName, mimeType, content)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            boolean successful = response.statusCode() >= 200 && response.statusCode() < 300;
            return new WhatsAppMediaUploadResult(successful, response.statusCode(), successful ? text(objectMapper.readTree(response.body()).path("id")) : null, response.body());
        } catch (Exception exception) {
            return new WhatsAppMediaUploadResult(false, 0, null, exception.getMessage());
        }
    }

    @Override
    public WhatsAppMediaSendResult sendMedia(String phone, WhatsAppOutboundMediaType type, String mediaId, String caption, String fileName) {
        try {
            HttpResponse<String> response = post(mediaMessageBody(phone, type, mediaId, caption, fileName));
            boolean successful = response.statusCode() >= 200 && response.statusCode() < 300;
            return new WhatsAppMediaSendResult(successful, response.statusCode(), successful ? externalMessageId(response.body()) : null, response.body());
        } catch (Exception exception) {
            return new WhatsAppMediaSendResult(false, 0, null, exception.getMessage());
        }
    }

    private HttpResponse<String> post(Map<String, Object> body) throws Exception {
        String requestBody = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder(endpoint())
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + properties.accessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private URI endpoint() {
        return URI.create(GRAPH_BASE_URL + "/" + graphApiVersion() + "/" + properties.phoneNumberId() + "/messages");
    }

    private URI mediaEndpoint(String mediaId) {
        return URI.create(GRAPH_BASE_URL + "/" + graphApiVersion() + "/" + mediaId);
    }

    private URI mediaUploadEndpoint() {
        return URI.create(GRAPH_BASE_URL + "/" + graphApiVersion() + "/" + properties.phoneNumberId() + "/media");
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

    private Map<String, Object> textRequestBody(String phone, String content) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", phone);
        body.put("type", "text");
        body.put("text", Map.of("preview_url", false, "body", content));
        return body;
    }

    private Map<String, Object> mediaMessageBody(String phone, WhatsAppOutboundMediaType type, String mediaId, String caption, String fileName) {
        String objectKey = switch (type) {
            case IMAGE -> "image";
            case AUDIO -> "audio";
            case DOCUMENT -> "document";
        };
        Map<String, Object> media = new LinkedHashMap<>();
        media.put("id", mediaId);
        if (caption != null && !caption.isBlank() && type != WhatsAppOutboundMediaType.AUDIO) {
            media.put("caption", caption.trim());
        }
        if (type == WhatsAppOutboundMediaType.DOCUMENT && fileName != null && !fileName.isBlank()) {
            media.put("filename", fileName.trim());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", phone);
        body.put("type", objectKey);
        body.put(objectKey, media);
        return body;
    }

    private byte[] multipartBody(String boundary, String fileName, String mimeType, byte[] content) {
        String normalizedMimeType = mimeType == null || mimeType.isBlank() ? "application/octet-stream" : mimeType;
        String normalizedFileName = fileName == null || fileName.isBlank() ? "media.bin" : fileName;
        String head = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"messaging_product\"\r\n\r\n"
                + "whatsapp\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + normalizedFileName.replace("\"", "_") + "\"\r\n"
                + "Content-Type: " + normalizedMimeType + "\r\n\r\n";
        String tail = "\r\n--" + boundary + "--\r\n";
        byte[] headBytes = head.getBytes(StandardCharsets.UTF_8);
        byte[] tailBytes = tail.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[headBytes.length + content.length + tailBytes.length];
        System.arraycopy(headBytes, 0, result, 0, headBytes.length);
        System.arraycopy(content, 0, result, headBytes.length, content.length);
        System.arraycopy(tailBytes, 0, result, headBytes.length + content.length, tailBytes.length);
        return result;
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

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
