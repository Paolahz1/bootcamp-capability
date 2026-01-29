package com.bootcamp.capabilityservice.infrastructure.config;

import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.IBootcampReportPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.IPersonClientPort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import com.bootcamp.capabilityservice.domain.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans para inyección de dependencias.
 * Define los casos de uso con sus dependencias de puertos.
 */
@Configuration
public class BeanConfiguration {

    // ==================== Capability Use Cases ====================

    @Bean
    public CreateCapabilityUseCase createCapabilityUseCase(
            ICapabilityPersistencePort persistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new CreateCapabilityUseCase(persistencePort, technologyClientPort);
    }

    @Bean
    public ListCapabilitiesUseCase listCapabilitiesUseCase(
            ICapabilityPersistencePort persistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new ListCapabilitiesUseCase(persistencePort, technologyClientPort);
    }

    @Bean
    public GetCapabilityByIdUseCase getCapabilityByIdUseCase(
            ICapabilityPersistencePort persistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new GetCapabilityByIdUseCase(persistencePort, technologyClientPort);
    }

    @Bean
    public GetCapabilitiesByIdsUseCase getCapabilitiesByIdsUseCase(
            ICapabilityPersistencePort persistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new GetCapabilitiesByIdsUseCase(persistencePort, technologyClientPort);
    }

    @Bean
    public CountCapabilitiesByTechnologyUseCase countCapabilitiesByTechnologyUseCase(
            ICapabilityPersistencePort persistencePort) {
        return new CountCapabilitiesByTechnologyUseCase(persistencePort);
    }

    @Bean
    public DeleteCapabilityUseCase deleteCapabilityUseCase(
            ICapabilityPersistencePort persistencePort,
            IBootcampClientPort bootcampClientPort) {
        return new DeleteCapabilityUseCase(persistencePort, bootcampClientPort);
    }

    // ==================== Bootcamp Orchestration Use Cases ====================

    @Bean
    public CreateBootcampUseCase createBootcampUseCase(
            ICapabilityPersistencePort capabilityPersistencePort,
            IBootcampClientPort bootcampClientPort,
            IBootcampReportPersistencePort reportPersistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new CreateBootcampUseCase(capabilityPersistencePort, bootcampClientPort, 
            reportPersistencePort, technologyClientPort);
    }

    @Bean
    public ListBootcampsUseCase listBootcampsUseCase(
            IBootcampClientPort bootcampClientPort,
            ICapabilityPersistencePort capabilityPersistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new ListBootcampsUseCase(bootcampClientPort, capabilityPersistencePort, technologyClientPort);
    }

    @Bean
    public DeleteBootcampSagaUseCase deleteBootcampSagaUseCase(
            IPersonClientPort personClientPort,
            IBootcampClientPort bootcampClientPort,
            ICapabilityPersistencePort capabilityPersistencePort) {
        return new DeleteBootcampSagaUseCase(personClientPort, bootcampClientPort, capabilityPersistencePort);
    }

    @Bean
    public EnrollPersonUseCase enrollPersonUseCase(
            IBootcampClientPort bootcampClientPort,
            IBootcampReportPersistencePort reportPersistencePort) {
        return new EnrollPersonUseCase(bootcampClientPort, reportPersistencePort);
    }

    // ==================== Report Use Cases ====================

    @Bean
    public GetTopBootcampUseCase getTopBootcampUseCase(
            IBootcampClientPort bootcampClientPort,
            ICapabilityPersistencePort capabilityPersistencePort,
            ITechnologyClientPort technologyClientPort) {
        return new GetTopBootcampUseCase(bootcampClientPort, capabilityPersistencePort, technologyClientPort);
    }
}
