package com.eai.api.openapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractTest {

    private static final String API_PACKAGE = "com.eai.api";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @DisplayName("Contrato OpenAPI e publico, valido e cobre endpoints da API")
    @Test
    void openApiContractIsPublicValidAndCoversApiEndpoints() throws Exception {
        JsonNode openApi = loadOpenApi();

        assertTrue(openApi.path("openapi").asText().startsWith("3."), "OpenAPI deve usar versao 3.x");
        assertEquals("EAI API", openApi.path("info").path("title").asText());
        assertEquals("v1", openApi.path("info").path("version").asText());

        JsonNode bearerAuth = openApi.path("components").path("securitySchemes").path("bearerAuth");
        assertEquals("http", bearerAuth.path("type").asText());
        assertEquals("bearer", bearerAuth.path("scheme").asText());
        assertEquals("JWT", bearerAuth.path("bearerFormat").asText());

        assertOpenApiDocumentsAllApiEndpoints(openApi);
        assertEveryOperationHasResponse(openApi);
        assertOperationIdsAreUnique(openApi);
    }

    private JsonNode loadOpenApi() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private void assertOpenApiDocumentsAllApiEndpoints(JsonNode openApi) {
        Set<ApiEndpoint> mappedEndpoints = mappedApiEndpoints();
        Set<ApiEndpoint> documentedEndpoints = documentedApiEndpoints(openApi);

        Set<ApiEndpoint> missingEndpoints = new TreeSet<>(mappedEndpoints);
        missingEndpoints.removeAll(documentedEndpoints);

        assertTrue(missingEndpoints.isEmpty(), "OpenAPI nao documenta endpoints registrados: " + missingEndpoints);
    }

    private Set<ApiEndpoint> mappedApiEndpoints() {
        Set<ApiEndpoint> endpoints = new TreeSet<>();

        handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
            if (!isEaiApiHandler(handlerMethod)) {
                return;
            }

            Set<String> paths = mappingPaths(mappingInfo);
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            if (methods.isEmpty()) {
                return;
            }

            for (String path : paths) {
                if (!path.startsWith("/api/")) {
                    continue;
                }
                for (RequestMethod method : methods) {
                    endpoints.add(new ApiEndpoint(method.name().toLowerCase(), path));
                }
            }
        });

        return endpoints;
    }

    private boolean isEaiApiHandler(HandlerMethod handlerMethod) {
        Package handlerPackage = handlerMethod.getBeanType().getPackage();
        return handlerPackage != null && handlerPackage.getName().startsWith(API_PACKAGE);
    }

    private Set<String> mappingPaths(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPathPatternsCondition() == null) {
            return Set.of();
        }

        Set<String> paths = new HashSet<>();
        for (PathPattern pattern : mappingInfo.getPathPatternsCondition().getPatterns()) {
            paths.add(pattern.getPatternString());
        }
        return paths;
    }

    private Set<ApiEndpoint> documentedApiEndpoints(JsonNode openApi) {
        Set<ApiEndpoint> endpoints = new TreeSet<>();
        JsonNode paths = openApi.path("paths");

        for (String path : paths.propertyNames()) {
            JsonNode pathItem = paths.path(path);
            for (String method : pathItem.propertyNames()) {
                if (isHttpMethod(method)) {
                    endpoints.add(new ApiEndpoint(method, path));
                }
            }
        }

        return endpoints;
    }

    private boolean isHttpMethod(String method) {
        return Set.of("get", "post", "put", "patch", "delete", "options", "head", "trace").contains(method);
    }

    private void assertEveryOperationHasResponse(JsonNode openApi) {
        forEachOperation(openApi, (endpoint, operation) ->
                assertFalse(operation.path("responses").isEmpty(), endpoint + " deve declarar responses"));
    }

    private void assertOperationIdsAreUnique(JsonNode openApi) {
        Set<String> operationIds = new HashSet<>();
        Set<String> duplicatedOperationIds = new TreeSet<>();

        forEachOperation(openApi, (endpoint, operation) -> {
            String operationId = operation.path("operationId").asText();
            assertFalse(operationId.isBlank(), endpoint + " deve declarar operationId");
            if (!operationIds.add(operationId)) {
                duplicatedOperationIds.add(operationId);
            }
        });

        assertTrue(duplicatedOperationIds.isEmpty(), "OpenAPI possui operationIds duplicados: " + duplicatedOperationIds);
    }

    private void forEachOperation(JsonNode openApi, OperationAssertion assertion) {
        JsonNode paths = openApi.path("paths");

        for (String path : paths.propertyNames()) {
            JsonNode pathItem = paths.path(path);
            for (String method : pathItem.propertyNames()) {
                if (isHttpMethod(method)) {
                    assertion.accept(new ApiEndpoint(method, path), pathItem.path(method));
                }
            }
        }
    }

    private record ApiEndpoint(String method, String path) implements Comparable<ApiEndpoint> {

        @Override
        public int compareTo(ApiEndpoint other) {
            int pathComparison = path.compareTo(other.path);
            if (pathComparison != 0) {
                return pathComparison;
            }
            return method.compareTo(other.method);
        }

        @Override
        public String toString() {
            return method.toUpperCase() + " " + path;
        }
    }

    @FunctionalInterface
    private interface OperationAssertion {

        void accept(ApiEndpoint endpoint, JsonNode operation);
    }
}
