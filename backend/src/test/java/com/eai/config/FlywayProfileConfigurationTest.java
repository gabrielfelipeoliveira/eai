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
        PropertySource<?> propertySource = loader.load(
                resourceName,
                resourceLoader.getResource("classpath:" + resourceName)
        ).getFirst();
        return (String) propertySource.getProperty("spring.flyway.locations");
    }
}
