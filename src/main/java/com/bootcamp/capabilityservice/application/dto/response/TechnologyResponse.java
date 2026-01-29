package com.bootcamp.capabilityservice.application.dto.response;

/**
 * DTO de respuesta para tecnología.
 */
public class TechnologyResponse {

    private Long id;
    private String name;
    private String description;

    public TechnologyResponse() {
    }

    public TechnologyResponse(Long id, String name, String description) {
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
