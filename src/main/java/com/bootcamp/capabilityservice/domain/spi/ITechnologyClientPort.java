package com.bootcamp.capabilityservice.domain.spi;

import com.bootcamp.capabilityservice.domain.model.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Puerto de salida para comunicación con Technology Service.
 * Define el contrato para obtener y validar tecnologías del servicio externo.
 */
public interface ITechnologyClientPort {

    /**
     * Valida que todas las tecnologías existan en Technology Service.
     *
     * @param technologyIds lista de IDs de tecnologías a validar
     * @return Mono con true si todas existen, false si alguna no existe
     */
    Mono<Boolean> validateTechnologiesExist(List<Long> technologyIds);

    /**
     * Obtiene los datos completos de múltiples tecnologías por sus IDs.
     *
     * @param technologyIds lista de IDs de tecnologías
     * @return Flux con las tecnologías encontradas
     */
    Flux<Technology> getTechnologiesByIds(List<Long> technologyIds);
}
