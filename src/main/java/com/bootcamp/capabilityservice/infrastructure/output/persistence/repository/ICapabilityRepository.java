package com.bootcamp.capabilityservice.infrastructure.output.persistence.repository;

import com.bootcamp.capabilityservice.infrastructure.output.persistence.entity.CapabilityEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Repositorio R2DBC para la entidad Capability.
 */
@Repository
public interface ICapabilityRepository extends ReactiveCrudRepository<CapabilityEntity, Long> {

    /**
     * Busca capacidades con paginación.
     */
    Flux<CapabilityEntity> findAllBy(Pageable pageable);

    /**
     * Verifica si existe una capacidad con el nombre dado.
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Busca capacidades por lista de IDs.
     */
    @Query("SELECT * FROM capabilities WHERE id IN (:ids)")
    Flux<CapabilityEntity> findByIdIn(List<Long> ids);

    /**
     * Busca una capacidad por nombre.
     */
    Mono<CapabilityEntity> findByName(String name);
}
