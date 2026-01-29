package com.bootcamp.capabilityservice.infrastructure.output.mongodb.repository;

import com.bootcamp.capabilityservice.infrastructure.output.mongodb.document.BootcampReportDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para documentos BootcampReport en MongoDB.
 * Extiende ReactiveMongoRepository para operaciones CRUD reactivas.
 */
public interface IBootcampReportRepository extends ReactiveMongoRepository<BootcampReportDocument, String> {

    /**
     * Busca un reporte de bootcamp por el ID del bootcamp.
     *
     * @param bootcampId ID del bootcamp en el sistema principal
     * @return Mono con el documento encontrado o vacío si no existe
     */
    Mono<BootcampReportDocument> findByBootcampId(Long bootcampId);

    /**
     * Obtiene el bootcamp con mayor cantidad de inscripciones.
     * Ordena por enrollmentCount descendente y retorna el primero.
     *
     * @return Mono con el documento del bootcamp más popular o vacío si no hay bootcamps
     */
    Mono<BootcampReportDocument> findFirstByOrderByEnrollmentCountDesc();

    /**
     * Elimina un reporte de bootcamp por el ID del bootcamp.
     *
     * @param bootcampId ID del bootcamp cuyo reporte se eliminará
     * @return Mono vacío al completar la eliminación
     */
    Mono<Void> deleteByBootcampId(Long bootcampId);
}
