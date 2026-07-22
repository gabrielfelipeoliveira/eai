package com.eai.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayProfileConfigurationTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    @DisplayName("Perfil prod executa apenas migrations comuns")
    @Test
    void prodFlywayLocationsExcludeSeedLocations() throws IOException {
        String locations = flywayLocations("application-prod.yml");

        assertThat(locations).isEqualTo("classpath:db/migration");
        assertThat(locations).doesNotContain("db/seed/mandatory", "db/seed/demo");
    }

    @DisplayName("Perfil prod desabilita OpenAPI publico por padrao")
    @Test
    void prodProfileDisablesPublicOpenApiByDefault() throws IOException {
        PropertySource<?> propertySource = yamlProperties("application-prod.yml");

        assertThat(propertySource.getProperty("springdoc.api-docs.enabled"))
                .isEqualTo("${SPRINGDOC_API_DOCS_ENABLED:false}");
        assertThat(propertySource.getProperty("springdoc.swagger-ui.enabled"))
                .isEqualTo("${SPRINGDOC_SWAGGER_UI_ENABLED:false}");
    }

    @DisplayName("Perfil prod exige origens CORS configuradas por ambiente")
    @Test
    void prodProfileRequiresCorsOriginsFromEnvironment() throws IOException {
        PropertySource<?> propertySource = yamlProperties("application-prod.yml");

        assertThat(propertySource.getProperty("eai.security.cors.allowed-origins"))
                .isEqualTo("${EAI_CORS_ALLOWED_ORIGINS}");
    }

    @DisplayName("Perfil demo habilita seed obrigatorio e massa demonstrativa explicitamente")
    @Test
    void demoFlywayLocationsIncludeMandatoryAndDemoSeeds() throws IOException {
        String locations = flywayLocations("application-demo.yml");

        assertThat(locations)
                .contains("classpath:db/migration")
                .contains("classpath:db/seed/mandatory")
                .contains("classpath:db/seed/demo");
    }

    private String flywayLocations(String resourceName) throws IOException {
        PropertySource<?> propertySource = yamlProperties(resourceName);
        return (String) propertySource.getProperty("spring.flyway.locations");
    }

    private PropertySource<?> yamlProperties(String resourceName) throws IOException {
        return loader.load(
                resourceName,
                resourceLoader.getResource("classpath:" + resourceName)
        ).getFirst();
    }
}
