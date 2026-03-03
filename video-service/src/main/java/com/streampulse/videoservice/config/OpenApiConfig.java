package com.streampulse.videoservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration for auto-generated API documentation.
 * Accessible at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI videoServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StreamPulse Video Service API")
                        .description("RESTful API for video content management, including CRUD operations, "
                                + "search/filtering, and TMDb metadata enrichment.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("StreamPulse Team")
                                .email("team@streampulse.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
