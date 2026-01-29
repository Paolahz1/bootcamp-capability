package com.bootcamp.capabilityservice.application.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para capacidad con tecnologías enriquecidas.
 */
public class CapabilityResponse {

    private Long id;
    private String name;
    private String description;
    private List<TechnologyResponse> technologies;
    private LocalDateTime createdAt;

    public CapabilityResponse() {
        this.technologies = new ArrayList<>();
    }

    public CapabilityResponse(Long id, String name, String description,
                              List<TechnologyResponse> technologies, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.technologies = technologies != null ? technologies : new ArrayList<>();
        this.createdAt = createdAt;
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

    public List<TechnologyResponse> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<TechnologyResponse> technologies) {
        this.technologies = technologies;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
