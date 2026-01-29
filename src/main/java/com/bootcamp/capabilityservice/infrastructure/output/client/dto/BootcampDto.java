package com.bootcamp.capabilityservice.infrastructure.output.client.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BootcampDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> capabilityIds;

    public BootcampDto() {
        this.capabilityIds = new ArrayList<>();
    }

    public BootcampDto(Long id, String name, String description, LocalDate startDate,
                       LocalDate endDate, List<Long> capabilityIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capabilityIds = capabilityIds != null ? capabilityIds : new ArrayList<>();
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

    public List<Long> getCapabilityIds() {
        return capabilityIds;
    }

    public void setCapabilityIds(List<Long> capabilityIds) {
        this.capabilityIds = capabilityIds;
    }
}
