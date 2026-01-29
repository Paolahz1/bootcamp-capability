package com.bootcamp.capabilityservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * Configuración para habilitar repositorios reactivos de MongoDB.
 * Escanea el paquete de repositorios MongoDB para detectar interfaces
 * que extienden ReactiveMongoRepository.
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.bootcamp.capabilityservice.infrastructure.output.mongodb.repository")
public class MongoConfiguration {
}
