package com.petstore.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI petStoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Paws & Claws Pet Store API")
                        .description("REST API documentation for the Pet Store E-Commerce Platform (V1). "
                                + "Provides endpoints for customer authentication, hierarchical category browsing (Dogs & Cats), "
                                + "product catalog with dynamic filtering, shopping cart, delivery address management, "
                                + "and concurrency-safe Cash on Delivery (COD) checkout.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Paws & Claws Engineering")
                                .email("support@pawsandclaws.store"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token to access protected customer endpoints.")));
    }
}
