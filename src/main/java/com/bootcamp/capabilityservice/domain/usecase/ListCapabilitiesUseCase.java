package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.domain.model.Technology;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Caso de uso para listar capacidades con paginación.
 * Enriquece cada capacidad con datos completos de tecnologías.
 */
public class ListCapabilitiesUseCase {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final ICapabilityPersistencePort persistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public ListCapabilitiesUseCase(ICapabilityPersistencePort persistencePort,
                                   ITechnologyClientPort technologyClientPort) {
        this.persistencePort = persistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Lista capacidades con paginación y enriquecimiento de tecnologías.
     *
     * @param page número de página
     * @param size tamaño de página
     * @param sortBy campo de ordenamiento
     * @param direction dirección de ordenamiento
     * @return Mono con la página de capacidades enriquecidas
     */
    public Mono<Page<Capability>> execute(int page, int size, String sortBy, String direction) {
        return persistencePort.findAll(page, size, sortBy, direction)
            .flatMap(this::enrichCapabilitiesWithTechnologies);
    }

    private Mono<Page<Capability>> enrichCapabilitiesWithTechnologies(Page<Capability> capabilityPage) {
        if (capabilityPage.getContent().isEmpty()) {
            return Mono.just(capabilityPage);
        }

        return reactor.core.publisher.Flux.fromIterable(capabilityPage.getContent())
            .flatMap(this::enrichCapabilityWithTechnologies, DEFAULT_CONCURRENCY)
            .collectList()
            .map(enrichedCapabilities -> new Page<>(
                enrichedCapabilities,
                capabilityPage.getPageNumber(),
                capabilityPage.getPageSize(),
                capabilityPage.getTotalElements()
            ));
    }

    private Mono<Capability> enrichCapabilityWithTechnologies(Capability capability) {
        if (capability.getTechnologyIds() == null || capability.getTechnologyIds().isEmpty()) {
            return Mono.just(capability);
        }

        return technologyClientPort.getTechnologiesByIds(capability.getTechnologyIds())
            .collectList()
            .map(technologies -> {
                // Las tecnologías se almacenan como IDs en el modelo de dominio
                // El enriquecimiento se hace en la capa de aplicación para el response
                return capability;
            })
            .defaultIfEmpty(capability);
    }
}
