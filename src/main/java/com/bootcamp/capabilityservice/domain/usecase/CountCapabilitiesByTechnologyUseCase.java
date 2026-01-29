package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para contar capacidades que usan una tecnología específica.
 * Usado por Technology Service para validar si una tecnología puede ser eliminada.
 */
public class CountCapabilitiesByTechnologyUseCase {

    private final ICapabilityPersistencePort persistencePort;

    public CountCapabilitiesByTechnologyUseCase(ICapabilityPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * Cuenta cuántas capacidades usan una tecnología específica.
     *
     * @param technologyId ID de la tecnología
     * @return Mono con el conteo de capacidades
     */
    public Mono<Long> execute(Long technologyId) {
        return persistencePort.countByTechnologyId(technologyId);
    }
}
