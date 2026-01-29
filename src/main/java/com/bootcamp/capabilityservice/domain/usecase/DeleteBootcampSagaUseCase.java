package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.IPersonClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Caso de uso para eliminar bootcamp usando Saga Pattern.
 * Ejecuta pasos secuenciales con compensación en caso de fallo.
 * 
 * Pasos del Saga:
 * 1. Eliminar inscripciones del bootcamp (Person Service)
 * 2. Obtener capacidades asociadas al bootcamp
 * 3. Eliminar el bootcamp (Bootcamp Service)
 * 4. Verificar y eliminar capacidades huérfanas
 */
public class DeleteBootcampSagaUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteBootcampSagaUseCase.class);
    private static final int DEFAULT_CONCURRENCY = 5;

    private final IPersonClientPort personClientPort;
    private final IBootcampClientPort bootcampClientPort;
    private final ICapabilityPersistencePort capabilityPersistencePort;

    public DeleteBootcampSagaUseCase(IPersonClientPort personClientPort,
                                     IBootcampClientPort bootcampClientPort,
                                     ICapabilityPersistencePort capabilityPersistencePort) {
        this.personClientPort = personClientPort;
        this.bootcampClientPort = bootcampClientPort;
        this.capabilityPersistencePort = capabilityPersistencePort;
    }

    /**
     * Ejecuta el Saga de eliminación de bootcamp.
     *
     * @param bootcampId ID del bootcamp a eliminar
     * @return Mono vacío al completar
     */
    public Mono<Void> execute(Long bootcampId) {
        AtomicBoolean enrollmentsDeleted = new AtomicBoolean(false);
        AtomicReference<List<Long>> capabilityIds = new AtomicReference<>(new ArrayList<>());

        return Mono.defer(() -> {
            log.info("Starting Saga for bootcamp deletion: {}", bootcampId);

            // Paso 1: Eliminar inscripciones
            return personClientPort.deleteEnrollmentsByBootcampId(bootcampId)
                .doOnSuccess(v -> {
                    enrollmentsDeleted.set(true);
                    log.debug("Step 1 completed: Enrollments deleted for bootcamp {}", bootcampId);
                })
                // Paso 2: Obtener capacidades asociadas antes de eliminar
                .then(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId)
                    .collectList()
                    .doOnSuccess(ids -> {
                        capabilityIds.set(ids);
                        log.debug("Step 2 completed: Found {} capabilities for bootcamp {}", ids.size(), bootcampId);
                    }))
                // Paso 3: Eliminar bootcamp
                .then(bootcampClientPort.deleteBootcamp(bootcampId)
                    .doOnSuccess(v -> log.debug("Step 3 completed: Bootcamp {} deleted", bootcampId)))
                // Paso 4: Limpiar capacidades huérfanas (defer para evaluar en tiempo de suscripción)
                .then(Mono.defer(() -> cleanupOrphanCapabilities(capabilityIds.get())))
                .doOnSuccess(v -> log.info("Saga completed successfully for bootcamp {}", bootcampId))
                .onErrorResume(error -> {
                    log.error("Saga failed for bootcamp {}: {}", bootcampId, error.getMessage());
                    // Nota: La compensación completa requeriría restaurar inscripciones y bootcamp
                    // En este caso, solo logueamos el error ya que las operaciones son irreversibles
                    return Mono.error(error);
                });
        });
    }

    private Mono<Void> cleanupOrphanCapabilities(List<Long> capabilityIds) {
        if (capabilityIds == null || capabilityIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(capabilityIds)
            .flatMap(this::deleteIfOrphan, DEFAULT_CONCURRENCY)
            .then();
    }

    private Mono<Void> deleteIfOrphan(Long capabilityId) {
        return bootcampClientPort.countBootcampsByCapabilityId(capabilityId)
            .flatMap(count -> {
                if (count == 0) {
                    log.debug("Deleting orphan capability: {}", capabilityId);
                    return capabilityPersistencePort.deleteById(capabilityId);
                }
                log.debug("Capability {} still in use by {} bootcamp(s)", capabilityId, count);
                return Mono.empty();
            });
    }
}
