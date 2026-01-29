package com.bootcamp.capabilityservice.application.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para bootcamp con capacidades enriquecidas.
 */
public class BootcampResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CapabilityResponse> capabilities;

    public BootcampResponse() {
        this.capabilities = new ArrayList<>();
    }

    public BootcampResponse(Long id, String name, String description, LocalDate startDate,
                            LocalDate endDate, List<CapabilityResponse> capabilities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capabilities = capabilities != null ? capabilities : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<CapabilityResponse> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<CapabilityResponse> capabilities) {
        this.capabilities = capabilities;
    }
}
