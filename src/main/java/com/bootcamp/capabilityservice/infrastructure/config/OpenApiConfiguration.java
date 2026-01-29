package com.bootcamp.capabilityservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación del API.
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Capability Service API")
                .version("1.0.0")
                .description("Microservicio orquestador para gestión de capacidades y coordinación de bootcamps. " +
                    "Implementado con Spring WebFlux para procesamiento reactivo.")
                .contact(new Contact()
                    .name("Bootcamp Team")
                    .email("bootcamp@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local Development Server")
            ));
    }
}
