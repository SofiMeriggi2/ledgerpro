package com.sofi.ledgerpro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
            .title("LedgerPro API")
            .description("API de cuentas, movimientos y transferencias")
            .version("v1")
            .contact(new Contact().name("Sofi")));
    }
}
