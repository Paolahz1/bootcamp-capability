package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para obtener el bootcamp con más inscripciones.
 * Enriquece el bootcamp con datos completos de capacidades y tecnologías.
 */
public class GetTopBootcampUseCase {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final IBootcampClientPort bootcampClientPort;
    private final ICapabilityPersistencePort capabilityPersistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public GetTopBootcampUseCase(IBootcampClientPort bootcampClientPort,
                                 ICapabilityPersistencePort capabilityPersistencePort,
                                 ITechnologyClientPort technologyClientPort) {
        this.bootcampClientPort = bootcampClientPort;
        this.capabilityPersistencePort = capabilityPersistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Obtiene el bootcamp con más inscripciones, enriquecido con capacidades.
     *
     * @return Mono con el bootcamp más popular
     */
    public Mono<Bootcamp> execute() {
        return bootcampClientPort.getTopBootcamp()
            .flatMap(this::enrichBootcampWithCapabilities);
    }

    private Mono<Bootcamp> enrichBootcampWithCapabilities(Bootcamp bootcamp) {
        if (bootcamp.getCapabilityIds() == null || bootcamp.getCapabilityIds().isEmpty()) {
            return Mono.just(bootcamp);
        }

        return capabilityPersistencePort.findByIds(bootcamp.getCapabilityIds())
            .flatMap(this::loadTechnologyIdsForCapability, DEFAULT_CONCURRENCY)
            .collectList()
            .map(capabilities -> {
                bootcamp.setCapabilities(capabilities);
                return bootcamp;
            });
    }

    private Mono<Capability> loadTechnologyIdsForCapability(Capability capability) {
        return capabilityPersistencePort.findTechnologyIdsByCapabilityId(capability.getId())
            .collectList()
            .map(technologyIds -> {
                capability.setTechnologyIds(technologyIds);
                return capability;
            });
    }
}
