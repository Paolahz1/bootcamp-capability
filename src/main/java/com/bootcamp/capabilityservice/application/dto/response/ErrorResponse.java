package com.bootcamp.capabilityservice.application.dto.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO de respuesta para errores.
 */
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> additionalInfo;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.additionalInfo = new HashMap<>();
    }

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.additionalInfo = new HashMap<>();
    }

    public ErrorResponse(int status, String message, Map<String, String> additionalInfo) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.additionalInfo = additionalInfo != null ? additionalInfo : new HashMap<>();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
