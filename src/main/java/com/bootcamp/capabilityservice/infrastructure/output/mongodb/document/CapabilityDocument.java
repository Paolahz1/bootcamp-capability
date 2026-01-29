package com.bootcamp.capabilityservice.infrastructure.output.mongodb.document;

import java.util.ArrayList;
import java.util.List;

/**
 * Documento embebido para capacidades dentro de BootcampReportDocument.
 * Almacena información desnormalizada de capacidades con sus tecnologías.
 */
public class CapabilityDocument {

    private Long id;
    private String name;
    private String description;
    private List<TechnologyDocument> technologies;

    public CapabilityDocument() {
        this.technologies = new ArrayList<>();
    }

    public CapabilityDocument(Long id, String name, String description, List<TechnologyDocument> technologies) {
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

    public List<TechnologyDocument> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<TechnologyDocument> technologies) {
        this.technologies = technologies;
    }
}
