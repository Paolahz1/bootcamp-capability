package com.bootcamp.capabilityservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Configuración para habilitar repositorios R2DBC.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.bootcamp.capabilityservice.infrastructure.output.persistence.repository")
public class R2dbcConfiguration {
}
