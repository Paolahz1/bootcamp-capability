package com.bootcamp.capabilityservice.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una capacidad.
 */
public class CapabilityNotFoundException extends DomainException {

    public CapabilityNotFoundException() {
        super(ExceptionResponse.CAPABILITY_NOT_FOUND);
    }

    public CapabilityNotFoundException(Long capabilityId) {
        super(ExceptionResponse.CAPABILITY_NOT_FOUND, "Capability ID: " + capabilityId);
    }
}
