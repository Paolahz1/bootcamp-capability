package com.bootcamp.capabilityservice.infrastructure.output.persistence.repository;

import com.bootcamp.capabilityservice.infrastructure.output.persistence.entity.CapabilityTechnologyEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la relación capability_technologies.
 */
@Repository
public interface ICapabilityTechnologyRepository extends ReactiveCrudRepository<CapabilityTechnologyEntity, Long> {

    /**
     * Busca todas las relaciones de una capacidad.
     */
    @Query("SELECT * FROM capability_technologies WHERE capability_id = :capabilityId")
    Flux<CapabilityTechnologyEntity> findByCapabilityId(Long capabilityId);

    /**
     * Obtiene los IDs de tecnologías de una capacidad.
     */
    @Query("SELECT technology_id FROM capability_technologies WHERE capability_id = :capabilityId")
    Flux<Long> findTechnologyIdsByCapabilityId(Long capabilityId);

    /**
     * Cuenta cuántas capacidades usan una tecnología.
     */
    @Query("SELECT COUNT(DISTINCT capability_id) FROM capability_technologies WHERE technology_id = :technologyId")
    Mono<Long> countByTechnologyId(Long technologyId);

    /**
     * Elimina todas las relaciones de una capacidad.
     */
    @Modifying
    @Query("DELETE FROM capability_technologies WHERE capability_id = :capabilityId")
    Mono<Void> deleteByCapabilityId(Long capabilityId);

    /**
     * Inserta una relación capability-technology.
     */
    @Modifying
    @Query("INSERT INTO capability_technologies (capability_id, technology_id) VALUES (:capabilityId, :technologyId)")
    Mono<Void> insertRelation(Long capabilityId, Long technologyId);
}
