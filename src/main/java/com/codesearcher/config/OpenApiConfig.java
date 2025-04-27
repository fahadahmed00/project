package com.codesearcher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GitHub Code Snippet Search Engine API")
                        .description("API for searching code snippets on GitHub, with history tracking and caching capabilities.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("University Project")
                                .url("https://github.com/codesearcher")
                                .email("student@university.edu"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}