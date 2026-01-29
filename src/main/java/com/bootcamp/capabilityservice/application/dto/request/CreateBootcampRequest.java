package com.bootcamp.capabilityservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para solicitud de creación de bootcamp.
 */
public class CreateBootcampRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Capability IDs are required")
    private List<Long> capabilityIds;

    public CreateBootcampRequest() {
    }

    public CreateBootcampRequest(String name, String description, LocalDate startDate,
                                 LocalDate endDate, List<Long> capabilityIds) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capabilityIds = capabilityIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Long> getCapabilityIds() {
        return capabilityIds;
    }

    public void setCapabilityIds(List<Long> capabilityIds) {
        this.capabilityIds = capabilityIds;
    }
}
