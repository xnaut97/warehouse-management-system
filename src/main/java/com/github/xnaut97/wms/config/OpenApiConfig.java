package com.github.xnaut97.wms.config;

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

    private static final String SECURITY_SCHEME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()

                .info(

                        new Info()

                                .title("Warehouse Management System API")

                                .description("""
                                        REST API for Warehouse Management System.

                                        Features:
                                        - Authentication
                                        - Warehouse Management
                                        - Supplier Management
                                        - Customer Management
                                        - Raw Material Management
                                        - Goods Receipt
                                        - Goods Issue
                                        - Inventory
                                        - Stocktaking
                                        - Dashboard
                                        - Reports
                                        - Audit Logs
                                        """)

                                .version("1.0.0")

                                .license(

                                        new License()

                                                .name("MIT")

                                )

                )

                .addSecurityItem(

                        new SecurityRequirement()

                                .addList(SECURITY_SCHEME)

                )

                .components(

                        new Components()

                                .addSecuritySchemes(

                                        SECURITY_SCHEME,

                                        new SecurityScheme()

                                                .name(SECURITY_SCHEME)

                                                .type(SecurityScheme.Type.HTTP)

                                                .scheme("bearer")

                                                .bearerFormat("JWT")

                                )

                );

    }

}