package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.exception.CapabilityNotFoundException;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para obtener una capacidad por su ID.
 * Enriquece la capacidad con datos completos de tecnologías.
 */
public class GetCapabilityByIdUseCase {

    private final ICapabilityPersistencePort persistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public GetCapabilityByIdUseCase(ICapabilityPersistencePort persistencePort,
                                    ITechnologyClientPort technologyClientPort) {
        this.persistencePort = persistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Obtiene una capacidad por su ID con tecnologías enriquecidas.
     *
     * @param id ID de la capacidad
     * @return Mono con la capacidad encontrada
     */
    public Mono<Capability> execute(Long id) {
        return persistencePort.findById(id)
            .switchIfEmpty(Mono.error(new CapabilityNotFoundException(id)))
            .flatMap(this::loadTechnologyIds);
    }

    private Mono<Capability> loadTechnologyIds(Capability capability) {
        return persistencePort.findTechnologyIdsByCapabilityId(capability.getId())
            .collectList()
            .map(technologyIds -> {
                capability.setTechnologyIds(technologyIds);
                return capability;
            });
    }
}
