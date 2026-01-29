package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Caso de uso para listar bootcamps con enriquecimiento de capacidades.
 * Obtiene bootcamps del Bootcamp Service y enriquece con datos de capacidades.
 */
public class ListBootcampsUseCase {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final IBootcampClientPort bootcampClientPort;
    private final ICapabilityPersistencePort capabilityPersistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public ListBootcampsUseCase(IBootcampClientPort bootcampClientPort,
                                ICapabilityPersistencePort capabilityPersistencePort,
                                ITechnologyClientPort technologyClientPort) {
        this.bootcampClientPort = bootcampClientPort;
        this.capabilityPersistencePort = capabilityPersistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Lista bootcamps con paginación y enriquecimiento de capacidades.
     *
     * @param page número de página
     * @param size tamaño de página
     * @param sortBy campo de ordenamiento
     * @param direction dirección de ordenamiento
     * @return Mono con la página de bootcamps enriquecidos
     */
    public Mono<Page<Bootcamp>> execute(int page, int size, String sortBy, String direction) {
        return bootcampClientPort.listBootcamps(page, size, sortBy, direction)
            .flatMap(this::enrichBootcampsWithCapabilities);
    }

    private Mono<Page<Bootcamp>> enrichBootcampsWithCapabilities(Page<Bootcamp> bootcampPage) {
        if (bootcampPage.getContent().isEmpty()) {
            return Mono.just(bootcampPage);
        }

        return Flux.fromIterable(bootcampPage.getContent())
            .flatMap(this::enrichBootcampWithCapabilities, DEFAULT_CONCURRENCY)
            .collectList()
            .map(enrichedBootcamps -> new Page<>(
                enrichedBootcamps,
                bootcampPage.getPageNumber(),
                bootcampPage.getPageSize(),
                bootcampPage.getTotalElements()
            ));
    }

    private Mono<Bootcamp> enrichBootcampWithCapabilities(Bootcamp bootcamp) {
        if (bootcamp.getCapabilityIds() == null || bootcamp.getCapabilityIds().isEmpty()) {
            return Mono.just(bootcamp);
        }

        return capabilityPersistencePort.findByIds(bootcamp.getCapabilityIds())
            .flatMap(this::loadTechnologyIdsForCapability)
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
