package com.bootcamp.capabilityservice.domain.model;

/**
 * Modelo de dominio para información desnormalizada de tecnologías en reportes.
 * Contiene los datos básicos de una tecnología para almacenar en MongoDB.
 */
public class TechnologyInfo {
    private Long id;
    private String name;
    private String description;

    public TechnologyInfo() {
    }

    public TechnologyInfo(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
}
