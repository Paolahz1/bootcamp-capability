package com.bootcamp.capabilityservice.domain.exception;

/**
 * Excepción lanzada cuando hay un error de comunicación con servicios externos.
 */
public class ExternalServiceException extends DomainException {

    public ExternalServiceException() {
        super(ExceptionResponse.EXTERNAL_SERVICE_ERROR);
    }

    public ExternalServiceException(String serviceName) {
        super(ExceptionResponse.EXTERNAL_SERVICE_ERROR, "Service: " + serviceName);
    }

    public ExternalServiceException(String serviceName, String details) {
        super(ExceptionResponse.EXTERNAL_SERVICE_ERROR, 
              "Service: " + serviceName + ", Details: " + details);
    }
}
