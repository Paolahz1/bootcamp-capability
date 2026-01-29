package com.bootcamp.capabilityservice.domain.api;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Puerto de entrada para operaciones CRUD de capacidades.
 * Define el contrato que la capa de aplicación usa para interactuar con la lógica de dominio.
 */
public interface ICapabilityServicePort {

    /**
     * Crea una nueva capacidad con las tecnologías asociadas.
     *
     * @param capability la capacidad a crear
     * @param technologyIds lista de IDs de tecnologías (debe contener entre 3 y 20 elementos sin duplicados)
     * @return Mono con la capacidad creada
     */
    Mono<Capability> createCapability(Capability capability, List<Long> technologyIds);

    /**
     * Lista capacidades con paginación y ordenamiento.
     *
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el cual ordenar
     * @param direction dirección del ordenamiento (ASC o DESC)
     * @return Mono con la página de capacidades
     */
    Mono<Page<Capability>> listCapabilities(int page, int size, String sortBy, String direction);

    /**
     * Obtiene una capacidad por su ID.
     *
     * @param id ID de la capacidad
     * @return Mono con la capacidad encontrada
     */
    Mono<Capability> getCapabilityById(Long id);

    /**
     * Obtiene múltiples capacidades por sus IDs.
     *
     * @param ids lista de IDs de capacidades
     * @return Flux con las capacidades encontradas (omite IDs inexistentes)
     */
    Flux<Capability> getCapabilitiesByIds(List<Long> ids);

    /**
     * Cuenta cuántas capacidades usan una tecnología específica.
     *
     * @param technologyId ID de la tecnología
     * @return Mono con el conteo de capacidades
     */
    Mono<Long> countCapabilitiesByTechnology(Long technologyId);

    /**
     * Elimina una capacidad por su ID.
     *
     * @param id ID de la capacidad a eliminar
     * @return Mono vacío al completar
     */
    Mono<Void> deleteCapability(Long id);
}
