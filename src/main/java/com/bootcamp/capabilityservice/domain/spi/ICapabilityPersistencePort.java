package com.bootcamp.capabilityservice.domain.spi;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Puerto de salida para persistencia de capacidades.
 * Define el contrato que los adaptadores de persistencia deben implementar.
 */
public interface ICapabilityPersistencePort {

    /**
     * Guarda una capacidad con sus tecnologías asociadas de forma atómica.
     *
     * @param capability la capacidad a guardar
     * @param technologyIds lista de IDs de tecnologías a asociar
     * @return Mono con la capacidad guardada
     */
    Mono<Capability> save(Capability capability, List<Long> technologyIds);

    /**
     * Obtiene todas las capacidades con paginación y ordenamiento.
     *
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el cual ordenar
     * @param direction dirección del ordenamiento (ASC o DESC)
     * @return Mono con la página de capacidades
     */
    Mono<Page<Capability>> findAll(int page, int size, String sortBy, String direction);

    /**
     * Busca una capacidad por su ID.
     *
     * @param id ID de la capacidad
     * @return Mono con la capacidad encontrada o vacío si no existe
     */
    Mono<Capability> findById(Long id);

    /**
     * Busca múltiples capacidades por sus IDs.
     *
     * @param ids lista de IDs de capacidades
     * @return Flux con las capacidades encontradas
     */
    Flux<Capability> findByIds(List<Long> ids);

    /**
     * Verifica si existe una capacidad con el nombre dado.
     *
     * @param name nombre a verificar
     * @return Mono con true si existe, false si no
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Cuenta cuántas capacidades usan una tecnología específica.
     *
     * @param technologyId ID de la tecnología
     * @return Mono con el conteo
     */
    Mono<Long> countByTechnologyId(Long technologyId);

    /**
     * Elimina una capacidad por su ID.
     *
     * @param id ID de la capacidad a eliminar
     * @return Mono vacío al completar
     */
    Mono<Void> deleteById(Long id);

    /**
     * Obtiene los IDs de tecnologías asociadas a una capacidad.
     *
     * @param capabilityId ID de la capacidad
     * @return Flux con los IDs de tecnologías
     */
    Flux<Long> findTechnologyIdsByCapabilityId(Long capabilityId);
}
