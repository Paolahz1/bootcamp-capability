package com.bootcamp.capabilityservice.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitud de inscripción de persona en bootcamp.
 */
public class EnrollmentRequest {

    @NotNull(message = "Bootcamp ID is required")
    private Long bootcampId;

    @NotNull(message = "Person ID is required")
    private Long personId;

    public EnrollmentRequest() {
    }

    public EnrollmentRequest(Long bootcampId, Long personId) {
        this.bootcampId = bootcampId;
        this.personId = personId;
    }

    public Long getBootcampId() {
        return bootcampId;
    }

    public void setBootcampId(Long bootcampId) {
        this.bootcampId = bootcampId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}
