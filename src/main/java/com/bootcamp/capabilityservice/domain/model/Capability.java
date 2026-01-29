package com.bootcamp.capabilityservice.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Capability {
    private Long id;
    private String name;
    private String description;
    private List<Long> technologyIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Capability() {
        this.technologyIds = new ArrayList<>();
    }

    public Capability(Long id, String name, String description, List<Long> technologyIds,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.technologyIds = technologyIds != null ? technologyIds : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public List<Long> getTechnologyIds() {
        return technologyIds;
    }

    public void setTechnologyIds(List<Long> technologyIds) {
        this.technologyIds = technologyIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
