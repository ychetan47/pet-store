package com.petstore.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pet Store Inventory Service API")
                        .description("Microservice managing inventory items, atomic stock reservations, releases, and outbox event publishing")
                        .version("3.0.0")
                        .contact(new Contact().name("Paws & Claws Platform Team")));
    }
}
