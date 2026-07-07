package com.eai.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eaiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EAI API")
                        .description("Automotive Lead Intelligence API")
                        .version("v1"));
    }
}
