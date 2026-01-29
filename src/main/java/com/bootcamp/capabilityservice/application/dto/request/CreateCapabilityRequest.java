package com.bootcamp.capabilityservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para solicitud de creación de capacidad.
 */
public class CreateCapabilityRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Technology IDs are required")
    @Size(min = 3, max = 20, message = "Capability must have between 3 and 20 technologies")
    private List<Long> technologyIds;

    public CreateCapabilityRequest() {
    }

    public CreateCapabilityRequest(String name, String description, List<Long> technologyIds) {
        this.name = name;
        this.description = description;
        this.technologyIds = technologyIds;
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

    public List<Long> getTechnologyIds() {
        return technologyIds;
    }

    public void setTechnologyIds(List<Long> technologyIds) {
        this.technologyIds = technologyIds;
    }
}
