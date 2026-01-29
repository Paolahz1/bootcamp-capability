package com.bootcamp.capabilityservice.domain.spi;

import com.bootcamp.capabilityservice.domain.model.BootcampReport;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de reportes de bootcamps en MongoDB.
 * Define el contrato que los adaptadores de persistencia MongoDB deben implementar.
 */
public interface IBootcampReportPersistencePort {

    /**
     * Guarda un reporte de bootcamp en MongoDB.
     *
     * @param report el reporte de bootcamp a guardar
     * @return Mono con el reporte guardado incluyendo el ID generado
     */
    Mono<BootcampReport> save(BootcampReport report);

    /**
     * Busca un reporte de bootcamp por el ID del bootcamp.
     *
     * @param bootcampId ID del bootcamp en el sistema principal
     * @return Mono con el reporte encontrado o vacío si no existe
     */
    Mono<BootcampReport> findByBootcampId(Long bootcampId);

    /**
     * Obtiene el bootcamp con mayor cantidad de inscripciones.
     *
     * @return Mono con el reporte del bootcamp más popular o vacío si no hay bootcamps
     */
    Mono<BootcampReport> findTopByEnrollmentCount();

    /**
     * Incrementa el contador de inscripciones de un bootcamp.
     *
     * @param bootcampId ID del bootcamp cuyo contador se incrementará
     * @return Mono vacío al completar la operación
     */
    Mono<Void> incrementEnrollmentCount(Long bootcampId);

    /**
     * Elimina un reporte de bootcamp por el ID del bootcamp.
     *
     * @param bootcampId ID del bootcamp cuyo reporte se eliminará
     * @return Mono vacío al completar la eliminación
     */
    Mono<Void> deleteByBootcampId(Long bootcampId);
}
