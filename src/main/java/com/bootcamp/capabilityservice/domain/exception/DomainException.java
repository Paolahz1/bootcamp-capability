package com.bootcamp.capabilityservice.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class DomainException extends RuntimeException {

    private final ExceptionResponse exceptionResponse;
    private final Map<String, String> additionalInfo;

    public DomainException(ExceptionResponse exceptionResponse) {
        super(exceptionResponse.getMessage());
        this.exceptionResponse = exceptionResponse;
        this.additionalInfo = new HashMap<>();
    }

    public DomainException(ExceptionResponse exceptionResponse, String details) {
        super(exceptionResponse.getMessage());
        this.exceptionResponse = exceptionResponse;
        this.additionalInfo = new HashMap<>();
        this.additionalInfo.put("details", details);
    }

    public DomainException(ExceptionResponse exceptionResponse, Map<String, String> additionalInfo) {
        super(exceptionResponse.getMessage());
        this.exceptionResponse = exceptionResponse;
        this.additionalInfo = additionalInfo != null ? additionalInfo : new HashMap<>();
    }

    public ExceptionResponse getExceptionResponse() {
        return exceptionResponse;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }
}
