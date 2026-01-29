package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.IBootcampReportPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Caso de uso para inscribir una persona en un bootcamp.
 * Delega la operación al Bootcamp Service e incrementa el contador de inscripciones en MongoDB.
 */
public class EnrollPersonUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnrollPersonUseCase.class);

    private final IBootcampClientPort bootcampClientPort;
    private final IBootcampReportPersistencePort reportPersistencePort;

    public EnrollPersonUseCase(IBootcampClientPort bootcampClientPort,
                               IBootcampReportPersistencePort reportPersistencePort) {
        this.bootcampClientPort = bootcampClientPort;
        this.reportPersistencePort = reportPersistencePort;
    }

    /**
     * Inscribe una persona en un bootcamp.
     * Incrementa el contador de inscripciones en MongoDB de forma asíncrona (fire-and-forget).
     *
     * @param bootcampId ID del bootcamp
     * @param personId ID de la persona
     * @return Mono vacío al completar
     */
    public Mono<Void> execute(Long bootcampId, Long personId) {
        return bootcampClientPort.enrollPerson(bootcampId, personId)
            .doOnSuccess(v -> {
                // Fire-and-forget: actualiza contador en MongoDB sin bloquear
                reportPersistencePort.incrementEnrollmentCount(bootcampId)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                        success -> log.debug("Enrollment count updated for bootcamp {}", bootcampId),
                        error -> log.error("Error updating enrollment count for bootcamp {}: {}", 
                            bootcampId, error.getMessage())
                    );
            });
    }
}
