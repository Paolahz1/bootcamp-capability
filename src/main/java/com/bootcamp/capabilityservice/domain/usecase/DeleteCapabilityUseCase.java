package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.exception.CapabilityInUseException;
import com.bootcamp.capabilityservice.domain.exception.CapabilityNotFoundException;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para eliminar una capacidad.
 * Valida que la capacidad no esté en uso por ningún bootcamp antes de eliminar.
 */
public class DeleteCapabilityUseCase {

    private final ICapabilityPersistencePort persistencePort;
    private final IBootcampClientPort bootcampClientPort;

    public DeleteCapabilityUseCase(ICapabilityPersistencePort persistencePort,
                                   IBootcampClientPort bootcampClientPort) {
        this.persistencePort = persistencePort;
        this.bootcampClientPort = bootcampClientPort;
    }

    /**
     * Elimina una capacidad por su ID.
     * Valida que exista y que no esté siendo usada por bootcamps.
     *
     * @param id ID de la capacidad a eliminar
     * @return Mono vacío al completar
     */
    public Mono<Void> execute(Long id) {
        return validateCapabilityExists(id)
            .then(validateCapabilityNotInUse(id))
            .then(persistencePort.deleteById(id));
    }

    private Mono<Void> validateCapabilityExists(Long id) {
        return persistencePort.findById(id)
            .switchIfEmpty(Mono.error(new CapabilityNotFoundException(id)))
            .then();
    }

    private Mono<Void> validateCapabilityNotInUse(Long id) {
        return bootcampClientPort.countBootcampsByCapabilityId(id)
            .flatMap(count -> {
                if (count > 0) {
                    return Mono.error(new CapabilityInUseException(id, count));
                }
                return Mono.empty();
            });
    }
}
