package com.bootcamp.capabilityservice.domain.exception;

/**
 * Excepción lanzada cuando los datos de una capacidad son inválidos.
 */
public class InvalidCapabilityException extends DomainException {

    public InvalidCapabilityException() {
        super(ExceptionResponse.INVALID_CAPABILITY);
    }

    public InvalidCapabilityException(String message) {
        super(ExceptionResponse.INVALID_CAPABILITY, message);
    }

    public InvalidCapabilityException(ExceptionResponse response) {
        super(response);
    }

    public InvalidCapabilityException(ExceptionResponse response, String details) {
        super(response, details);
    }
}
