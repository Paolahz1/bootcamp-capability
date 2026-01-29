package com.bootcamp.capabilityservice.domain.exception;

import org.springframework.http.HttpStatus;

public enum ExceptionResponse {
    CAPABILITY_NOT_FOUND("Capability not found", HttpStatus.NOT_FOUND),
    INVALID_CAPABILITY("Invalid capability data", HttpStatus.BAD_REQUEST),
    DUPLICATE_NAME("Capability name already exists", HttpStatus.BAD_REQUEST),
    INVALID_TECHNOLOGY_COUNT("Capability must have between 3 and 20 technologies", HttpStatus.BAD_REQUEST),
    DUPLICATE_TECHNOLOGIES("Duplicate technology IDs not allowed", HttpStatus.BAD_REQUEST),
    TECHNOLOGIES_NOT_FOUND("One or more technologies do not exist", HttpStatus.BAD_REQUEST),
    DELETE_FAILED("Cannot delete capability in use by bootcamps", HttpStatus.BAD_REQUEST),
    EXTERNAL_SERVICE_ERROR("Error communicating with external service", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;

    ExceptionResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
