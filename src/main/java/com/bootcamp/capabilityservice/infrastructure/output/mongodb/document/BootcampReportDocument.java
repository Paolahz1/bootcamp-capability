package com.bootcamp.capabilityservice.infrastructure.output.mongodb.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Documento MongoDB para almacenar reportes de bootcamps.
 * Contiene datos desnormalizados para consultas rápidas de métricas.
 */
@Document(collection = "bootcamp_reports")
public class BootcampReportDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long bootcampId;

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacityCount;
    private Integer technologyCount;

    @Indexed
    private Long enrollmentCount;

    private List<CapabilityDocument> capabilities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BootcampReportDocument() {
        this.capabilities = new ArrayList<>();
        this.enrollmentCount = 0L;
    }

    public BootcampReportDocument(String id, Long bootcampId, String name, String description,
                                   LocalDate startDate, LocalDate endDate, Integer capacityCount,
                                   Integer technologyCount, Long enrollmentCount,
                                   List<CapabilityDocument> capabilities,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bootcampId = bootcampId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacityCount = capacityCount;
        this.technologyCount = technologyCount;
        this.enrollmentCount = enrollmentCount != null ? enrollmentCount : 0L;
        this.capabilities = capabilities != null ? capabilities : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getBootcampId() {
        return bootcampId;
    }

    public void setBootcampId(Long bootcampId) {
        this.bootcampId = bootcampId;
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

    public Integer getCapacityCount() {
        return capacityCount;
    }

    public void setCapacityCount(Integer capacityCount) {
        this.capacityCount = capacityCount;
    }

    public Integer getTechnologyCount() {
        return technologyCount;
    }

    public void setTechnologyCount(Integer technologyCount) {
        this.technologyCount = technologyCount;
    }

    public Long getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(Long enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }

    public List<CapabilityDocument> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<CapabilityDocument> capabilities) {
        this.capabilities = capabilities;
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
