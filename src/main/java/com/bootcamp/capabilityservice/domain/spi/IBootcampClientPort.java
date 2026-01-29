package com.bootcamp.capabilityservice.domain.spi;

import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para comunicación con Bootcamp Service.
 * Define el contrato para operaciones de bootcamps en el servicio externo.
 */
public interface IBootcampClientPort {

    /**
     * Cuenta cuántos bootcamps usan una capacidad específica.
     *
     * @param capabilityId ID de la capacidad
     * @return Mono con el conteo de bootcamps
     */
    Mono<Long> countBootcampsByCapabilityId(Long capabilityId);

    /**
     * Crea un nuevo bootcamp en Bootcamp Service.
     *
     * @param bootcamp el bootcamp a crear
     * @return Mono con el bootcamp creado
     */
    Mono<Bootcamp> createBootcamp(Bootcamp bootcamp);

    /**
     * Lista bootcamps con paginación.
     *
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el cual ordenar
     * @param direction dirección del ordenamiento (ASC o DESC)
     * @return Mono con la página de bootcamps
     */
    Mono<Page<Bootcamp>> listBootcamps(int page, int size, String sortBy, String direction);

    /**
     * Elimina un bootcamp por su ID.
     *
     * @param bootcampId ID del bootcamp a eliminar
     * @return Mono vacío al completar
     */
    Mono<Void> deleteBootcamp(Long bootcampId);

    /**
     * Obtiene el bootcamp con más inscripciones.
     *
     * @return Mono con el bootcamp más popular
     */
    Mono<Bootcamp> getTopBootcamp();

    /**
     * Obtiene los IDs de capacidades asociadas a un bootcamp.
     *
     * @param bootcampId ID del bootcamp
     * @return Flux con los IDs de capacidades
     */
    Flux<Long> getCapabilityIdsByBootcampId(Long bootcampId);

    /**
     * Inscribe una persona en un bootcamp.
     *
     * @param bootcampId ID del bootcamp
     * @param personId ID de la persona
     * @return Mono vacío al completar
     */
    Mono<Void> enrollPerson(Long bootcampId, Long personId);
}
