package com.bootcamp.capabilityservice.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dominio para capacidades desnormalizadas con sus tecnologías.
 * Usado en reportes de MongoDB para almacenar información completa de capacidades.
 */
public class CapabilityWithTechnologies {
    private Long id;
    private String name;
    private String description;
    private List<TechnologyInfo> technologies;

    public CapabilityWithTechnologies() {
        this.technologies = new ArrayList<>();
    }

    public CapabilityWithTechnologies(Long id, String name, String description, List<TechnologyInfo> technologies) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.technologies = technologies != null ? technologies : new ArrayList<>();
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

    public List<TechnologyInfo> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<TechnologyInfo> technologies) {
        this.technologies = technologies;
    }
}
