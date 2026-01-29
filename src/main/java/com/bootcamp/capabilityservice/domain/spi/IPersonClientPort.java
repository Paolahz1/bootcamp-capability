package com.bootcamp.capabilityservice.domain.spi;

import com.bootcamp.capabilityservice.domain.model.PersonInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para comunicación con Person Service.
 * Define el contrato para operaciones de personas e inscripciones en el servicio externo.
 */
public interface IPersonClientPort {

    /**
     * Elimina todas las inscripciones de un bootcamp.
     * Usado en el Saga Pattern de eliminación de bootcamp.
     *
     * @param bootcampId ID del bootcamp
     * @return Mono vacío al completar
     */
    Mono<Void> deleteEnrollmentsByBootcampId(Long bootcampId);

    /**
     * Inscribe una persona en un bootcamp.
     *
     * @param bootcampId ID del bootcamp
     * @param personId ID de la persona
     * @return Mono vacío al completar
     */
    Mono<Void> enrollPerson(Long bootcampId, Long personId);

    /**
     * Obtiene la información de las personas inscritas en un bootcamp.
     * Retorna nombre y email de cada persona inscrita.
     *
     * @param bootcampId ID del bootcamp
     * @return Flux de PersonInfo con nombre y email de los inscritos
     */
    Flux<PersonInfo> getEnrolleesByBootcampId(Long bootcampId);
}
