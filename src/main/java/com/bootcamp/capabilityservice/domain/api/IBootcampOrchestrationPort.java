package com.bootcamp.capabilityservice.domain.api;

import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Page;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Puerto de entrada para operaciones de orquestación de bootcamps.
 * Define el contrato para coordinar operaciones entre Capability Service y servicios externos.
 */
public interface IBootcampOrchestrationPort {

    /**
     * Crea un nuevo bootcamp validando que las capacidades existan.
     *
     * @param bootcamp el bootcamp a crear
     * @param capabilityIds lista de IDs de capacidades que debe tener el bootcamp
     * @return Mono con el bootcamp creado
     */
    Mono<Bootcamp> createBootcamp(Bootcamp bootcamp, List<Long> capabilityIds);

    /**
     * Lista bootcamps con paginación, enriquecidos con datos de capacidades.
     *
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el cual ordenar
     * @param direction dirección del ordenamiento (ASC o DESC)
     * @return Mono con la página de bootcamps enriquecidos
     */
    Mono<Page<Bootcamp>> listBootcamps(int page, int size, String sortBy, String direction);

    /**
     * Elimina un bootcamp usando Saga Pattern con compensación.
     * Pasos: eliminar inscripciones -> eliminar bootcamp -> limpiar capacidades huérfanas.
     *
     * @param bootcampId ID del bootcamp a eliminar
     * @return Mono vacío al completar
     */
    Mono<Void> deleteBootcampWithSaga(Long bootcampId);

    /**
     * Inscribe una persona en un bootcamp delegando al Bootcamp Service.
     *
     * @param bootcampId ID del bootcamp
     * @param personId ID de la persona a inscribir
     * @return Mono vacío al completar
     */
    Mono<Void> enrollPersonInBootcamp(Long bootcampId, Long personId);
}
