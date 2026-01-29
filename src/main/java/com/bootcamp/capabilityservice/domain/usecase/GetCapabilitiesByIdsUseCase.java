package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Caso de uso para obtener múltiples capacidades por sus IDs.
 * Procesa las consultas en paralelo y omite IDs inexistentes.
 */
public class GetCapabilitiesByIdsUseCase {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final ICapabilityPersistencePort persistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public GetCapabilitiesByIdsUseCase(ICapabilityPersistencePort persistencePort,
                                       ITechnologyClientPort technologyClientPort) {
        this.persistencePort = persistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Obtiene múltiples capacidades por sus IDs con procesamiento paralelo.
     * Los IDs inexistentes son omitidos sin generar error.
     *
     * @param ids lista de IDs de capacidades
     * @return Flux con las capacidades encontradas
     */
    public Flux<Capability> execute(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        return persistencePort.findByIds(ids)
            .flatMap(this::loadTechnologyIds, DEFAULT_CONCURRENCY);
    }

    private Flux<Capability> loadTechnologyIds(Capability capability) {
        return persistencePort.findTechnologyIdsByCapabilityId(capability.getId())
            .collectList()
            .map(technologyIds -> {
                capability.setTechnologyIds(technologyIds);
                return capability;
            })
            .flux();
    }
}
