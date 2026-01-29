package com.bootcamp.capabilityservice.domain.exception;

/**
 * Excepción lanzada cuando se intenta eliminar una capacidad que está en uso.
 */
public class CapabilityInUseException extends DomainException {

    public CapabilityInUseException() {
        super(ExceptionResponse.DELETE_FAILED);
    }

    public CapabilityInUseException(String details) {
        super(ExceptionResponse.DELETE_FAILED, details);
    }

    public CapabilityInUseException(Long capabilityId, Long bootcampCount) {
        super(ExceptionResponse.DELETE_FAILED, 
              "Capability ID " + capabilityId + " is used by " + bootcampCount + " bootcamp(s)");
    }
}
