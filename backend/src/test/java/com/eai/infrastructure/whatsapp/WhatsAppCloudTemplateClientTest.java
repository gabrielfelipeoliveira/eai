package com.eai.infrastructure.whatsapp;

import com.eai.application.whatsapp.WhatsAppMediaDownload;
import com.eai.application.whatsapp.WhatsAppMediaMetadata;
import com.eai.application.whatsapp.WhatsAppMediaSendResult;
import com.eai.application.whatsapp.WhatsAppMediaUploadResult;
import com.eai.application.whatsapp.WhatsAppOutboundMediaType;
import com.eai.application.whatsapp.WhatsAppTemplateProviderResult;
import com.eai.application.whatsapp.WhatsAppTextProviderResult;
import com.eai.infrastructure.config.WhatsAppCloudApiProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhatsAppCloudTemplateClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<CapturedRequest> requests = new ArrayList<>();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @DisplayName("Envia template com parametros no corpo")
    @Test
    void sendsTemplateWithBodyParameters() throws Exception {
        startServer(200, "{\"messages\":[{\"id\":\"wamid-template\"}]}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppTemplateProviderResult result = client.sendTemplate(
                "5511999999999",
                "lead_novo",
                "pt_BR",
                List.of("Maria", "Civic")
        );

        assertThat(result.successful()).isTrue();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.externalMessageId()).isEqualTo("wamid-template");
        CapturedRequest request = requests.getFirst();
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/v99.0/phone-id/messages");
        assertThat(request.authorization()).isEqualTo("Bearer token");
        JsonNode body = objectMapper.readTree(request.body());
        assertThat(body.path("type").asString()).isEqualTo("template");
        assertThat(body.path("template").path("name").asString()).isEqualTo("lead_novo");
        assertThat(body.path("template").path("components").get(0).path("parameters").size()).isEqualTo(2);
    }

    @DisplayName("Envia texto simples sem preview de URL")
    @Test
    void sendsTextWithoutUrlPreview() throws Exception {
        startServer(201, "{\"messages\":[{\"id\":\"wamid-text\"}]}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppTextProviderResult result = client.sendText("5511888888888", "Ola");

        assertThat(result.successful()).isTrue();
        assertThat(result.statusCode()).isEqualTo(201);
        assertThat(result.externalMessageId()).isEqualTo("wamid-text");
        JsonNode body = objectMapper.readTree(requests.getFirst().body());
        assertThat(body.path("type").asString()).isEqualTo("text");
        assertThat(body.path("text").path("preview_url").asBoolean()).isFalse();
        assertThat(body.path("text").path("body").asString()).isEqualTo("Ola");
    }

    @DisplayName("Retorna falha quando envio de texto recebe erro do provedor")
    @Test
    void returnsFailureWhenTextProviderReturnsError() {
        startServer(400, "{\"error\":{\"message\":\"invalid phone\"}}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppTextProviderResult result = client.sendText("invalid", "Ola");

        assertThat(result.successful()).isFalse();
        assertThat(result.statusCode()).isEqualTo(400);
        assertThat(result.externalMessageId()).isNull();
        assertThat(result.rawResponse()).contains("invalid phone");
    }

    @DisplayName("Envia documento com legenda e nome de arquivo")
    @Test
    void sendsDocumentWithCaptionAndFileName() throws Exception {
        startServer(200, "{\"messages\":[{\"id\":\"wamid-document\"}]}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppMediaSendResult result = client.sendMedia(
                "5511777777777",
                WhatsAppOutboundMediaType.DOCUMENT,
                "media-id",
                " Proposta ",
                " proposta.pdf "
        );

        assertThat(result.successful()).isTrue();
        assertThat(result.externalMessageId()).isEqualTo("wamid-document");
        JsonNode body = objectMapper.readTree(requests.getFirst().body());
        assertThat(body.path("type").asString()).isEqualTo("document");
        assertThat(body.path("document").path("id").asString()).isEqualTo("media-id");
        assertThat(body.path("document").path("caption").asString()).isEqualTo("Proposta");
        assertThat(body.path("document").path("filename").asString()).isEqualTo("proposta.pdf");
    }

    @DisplayName("Nao envia legenda para audio")
    @Test
    void doesNotSendCaptionForAudio() throws Exception {
        startServer(200, "{\"messages\":[{\"id\":\"wamid-audio\"}]}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppMediaSendResult result = client.sendMedia(
                "5511666666666",
                WhatsAppOutboundMediaType.AUDIO,
                "audio-id",
                "Legenda ignorada",
                null
        );

        assertThat(result.successful()).isTrue();
        JsonNode body = objectMapper.readTree(requests.getFirst().body());
        assertThat(body.path("type").asString()).isEqualTo("audio");
        assertThat(body.path("audio").path("caption").isMissingNode()).isTrue();
    }

    @DisplayName("Busca metadata de midia do WhatsApp")
    @Test
    void fetchesMediaMetadata() {
        startServer(200, "{\"id\":\"media-id\",\"url\":\"http://example.test/media\",\"mime_type\":\"image/jpeg\",\"file_size\":123,\"sha256\":\"abc\"}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppMediaMetadata metadata = client.fetchMediaMetadata("media-id");

        assertThat(metadata.mediaId()).isEqualTo("media-id");
        assertThat(metadata.url()).isEqualTo("http://example.test/media");
        assertThat(metadata.mimeType()).isEqualTo("image/jpeg");
        assertThat(metadata.fileSizeBytes()).isEqualTo(123);
        assertThat(metadata.sha256()).isEqualTo("abc");
        assertThat(requests.getFirst().method()).isEqualTo("GET");
        assertThat(requests.getFirst().path()).isEqualTo("/v99.0/media-id");
    }

    @DisplayName("Falha ao buscar metadata quando provedor retorna erro")
    @Test
    void failsFetchingMetadataWhenProviderReturnsError() {
        startServer(500, "{\"error\":\"unavailable\"}");
        WhatsAppCloudTemplateClient client = client();

        assertThatThrownBy(() -> client.fetchMediaMetadata("media-id"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Could not fetch WhatsApp media metadata");
    }

    @DisplayName("Baixa conteudo binario de midia")
    @Test
    void downloadsMediaBytes() {
        startServer(200, "arquivo");
        WhatsAppCloudTemplateClient client = client();
        WhatsAppMediaMetadata metadata = new WhatsAppMediaMetadata("media-id", baseUrl() + "/download", "text/plain", 7L, "sha", "raw");

        WhatsAppMediaDownload download = client.downloadMedia(metadata);

        assertThat(download.content()).isEqualTo("arquivo".getBytes(StandardCharsets.UTF_8));
        assertThat(download.rawResponse()).isEqualTo("raw");
        assertThat(requests.getFirst().authorization()).isEqualTo("Bearer token");
    }

    @DisplayName("Faz upload multipart de midia com nome sanitizado")
    @Test
    void uploadsMultipartMediaWithSanitizedFileName() {
        startServer(200, "{\"id\":\"uploaded-media\"}");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppMediaUploadResult result = client.uploadMedia("arquivo\"final.jpg", "image/jpeg", "foto".getBytes(StandardCharsets.UTF_8));

        assertThat(result.successful()).isTrue();
        assertThat(result.mediaId()).isEqualTo("uploaded-media");
        CapturedRequest request = requests.getFirst();
        assertThat(request.path()).isEqualTo("/v99.0/phone-id/media");
        assertThat(request.contentType()).contains("multipart/form-data; boundary=");
        assertThat(request.body()).contains("name=\"messaging_product\"");
        assertThat(request.body()).contains("filename=\"arquivo_final.jpg\"");
        assertThat(request.body()).contains("Content-Type: image/jpeg");
        assertThat(request.body()).contains("foto");
    }

    @DisplayName("Retorna falha quando upload nao recebe JSON valido")
    @Test
    void returnsFailureWhenUploadResponseIsInvalid() {
        startServer(200, "invalid-json");
        WhatsAppCloudTemplateClient client = client();

        WhatsAppMediaUploadResult result = client.uploadMedia(null, null, "x".getBytes(StandardCharsets.UTF_8));

        assertThat(result.successful()).isFalse();
        assertThat(result.statusCode()).isEqualTo(0);
        assertThat(result.mediaId()).isNull();
    }

    private WhatsAppCloudTemplateClient client() {
        return new WhatsAppCloudTemplateClient(
                new WhatsAppCloudApiProperties("phone-id", null, "token", null, null, " v99.0 ", null, null),
                objectMapper,
                HttpClient.newHttpClient(),
                baseUrl()
        );
    }

    private void startServer(int statusCode, String responseBody) {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> handle(exchange, statusCode, responseBody));
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start test HTTP server", exception);
        }
    }

    private void handle(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] requestBody = exchange.getRequestBody().readAllBytes();
        requests.add(new CapturedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                exchange.getRequestHeaders().getFirst("Authorization"),
                exchange.getRequestHeaders().getFirst("Content-Type"),
                new String(requestBody, StandardCharsets.UTF_8)
        ));
        byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private record CapturedRequest(String method, String path, String authorization, String contentType, String body) {
    }
}
