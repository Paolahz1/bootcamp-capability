package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.api.ICapabilityServicePort;
import com.bootcamp.capabilityservice.domain.exception.ExceptionResponse;
import com.bootcamp.capabilityservice.domain.exception.InvalidCapabilityException;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Caso de uso para crear capacidades.
 * Implementa validaciones de negocio antes de persistir.
 */
public class CreateCapabilityUseCase {

    private static final int MIN_TECHNOLOGIES = 3;
    private static final int MAX_TECHNOLOGIES = 20;

    private final ICapabilityPersistencePort persistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public CreateCapabilityUseCase(ICapabilityPersistencePort persistencePort,
                                   ITechnologyClientPort technologyClientPort) {
        this.persistencePort = persistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Crea una nueva capacidad con las validaciones de negocio.
     *
     * @param capability la capacidad a crear
     * @param technologyIds lista de IDs de tecnologías
     * @return Mono con la capacidad creada
     */
    public Mono<Capability> execute(Capability capability, List<Long> technologyIds) {
        return validateTechnologyCount(technologyIds)
            .then(validateNoDuplicateTechnologies(technologyIds))
            .then(validateTechnologiesExist(technologyIds))
            .then(validateUniqueCapabilityName(capability.getName()))
            .then(persistencePort.save(capability, technologyIds));
    }

    private Mono<Void> validateTechnologyCount(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.size() < MIN_TECHNOLOGIES || 
            technologyIds.size() > MAX_TECHNOLOGIES) {
            return Mono.error(new InvalidCapabilityException(ExceptionResponse.INVALID_TECHNOLOGY_COUNT));
        }
        return Mono.empty();
    }

    private Mono<Void> validateNoDuplicateTechnologies(List<Long> technologyIds) {
        Set<Long> uniqueIds = new HashSet<>(technologyIds);
        if (uniqueIds.size() != technologyIds.size()) {
            return Mono.error(new InvalidCapabilityException(ExceptionResponse.DUPLICATE_TECHNOLOGIES));
        }
        return Mono.empty();
    }

    private Mono<Void> validateTechnologiesExist(List<Long> technologyIds) {
        return technologyClientPort.validateTechnologiesExist(technologyIds)
            .flatMap(allExist -> {
                if (!allExist) {
                    return Mono.error(new InvalidCapabilityException(ExceptionResponse.TECHNOLOGIES_NOT_FOUND));
                }
                return Mono.empty();
            });
    }

    private Mono<Void> validateUniqueCapabilityName(String name) {
        return persistencePort.existsByName(name)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new InvalidCapabilityException(ExceptionResponse.DUPLICATE_NAME));
                }
                return Mono.empty();
            });
    }
}
