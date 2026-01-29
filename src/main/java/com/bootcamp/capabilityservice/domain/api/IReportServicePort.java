package com.bootcamp.capabilityservice.domain.api;

import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para operaciones de reportes.
 * Define el contrato para generar reportes relacionados con bootcamps y capacidades.
 */
public interface IReportServicePort {

    /**
     * Obtiene el bootcamp con más personas inscritas, enriquecido con datos de capacidades.
     *
     * @return Mono con el bootcamp más popular
     */
    Mono<Bootcamp> getTopBootcamp();
}
