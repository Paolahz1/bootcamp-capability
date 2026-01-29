package com.bootcamp.capabilityservice.infrastructure.output.mongodb.mapper;

import com.bootcamp.capabilityservice.domain.model.BootcampReport;
import com.bootcamp.capabilityservice.domain.model.CapabilityWithTechnologies;
import com.bootcamp.capabilityservice.domain.model.TechnologyInfo;
import com.bootcamp.capabilityservice.infrastructure.output.mongodb.document.BootcampReportDocument;
import com.bootcamp.capabilityservice.infrastructure.output.mongodb.document.CapabilityDocument;
import com.bootcamp.capabilityservice.infrastructure.output.mongodb.document.TechnologyDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre documentos MongoDB y modelos de dominio.
 * Maneja la transformación bidireccional de BootcampReport y sus componentes.
 */
@Component
public class BootcampReportDocumentMapper {

    /**
     * Convierte un modelo de dominio BootcampReport a documento MongoDB.
     *
     * @param report modelo de dominio
     * @return documento MongoDB
     */
    public BootcampReportDocument toDocument(BootcampReport report) {
        if (report == null) {
            return null;
        }

        BootcampReportDocument document = new BootcampReportDocument();
        document.setId(report.getId());
        document.setBootcampId(report.getBootcampId());
        document.setName(report.getName());
        document.setDescription(report.getDescription());
        document.setStartDate(report.getStartDate());
        document.setEndDate(report.getEndDate());
        document.setCapacityCount(report.getCapacityCount());
        document.setTechnologyCount(report.getTechnologyCount());
        document.setEnrollmentCount(report.getEnrollmentCount());
        document.setCapabilities(toCapabilityDocuments(report.getCapabilities()));
        document.setCreatedAt(report.getCreatedAt());
        document.setUpdatedAt(report.getUpdatedAt());

        return document;
    }

    /**
     * Convierte un documento MongoDB a modelo de dominio BootcampReport.
     *
     * @param document documento MongoDB
     * @return modelo de dominio
     */
    public BootcampReport toDomain(BootcampReportDocument document) {
        if (document == null) {
            return null;
        }

        BootcampReport report = new BootcampReport();
        report.setId(document.getId());
        report.setBootcampId(document.getBootcampId());
        report.setName(document.getName());
        report.setDescription(document.getDescription());
        report.setStartDate(document.getStartDate());
        report.setEndDate(document.getEndDate());
        report.setCapacityCount(document.getCapacityCount());
        report.setTechnologyCount(document.getTechnologyCount());
        report.setEnrollmentCount(document.getEnrollmentCount());
        report.setCapabilities(toCapabilityDomains(document.getCapabilities()));
        report.setCreatedAt(document.getCreatedAt());
        report.setUpdatedAt(document.getUpdatedAt());

        return report;
    }

    private List<CapabilityDocument> toCapabilityDocuments(List<CapabilityWithTechnologies> capabilities) {
        if (capabilities == null) {
            return List.of();
        }

        return capabilities.stream()
                .map(this::toCapabilityDocument)
                .collect(Collectors.toList());
    }

    private CapabilityDocument toCapabilityDocument(CapabilityWithTechnologies capability) {
        if (capability == null) {
            return null;
        }

        CapabilityDocument document = new CapabilityDocument();
        document.setId(capability.getId());
        document.setName(capability.getName());
        document.setDescription(capability.getDescription());
        document.setTechnologies(toTechnologyDocuments(capability.getTechnologies()));

        return document;
    }

    private List<TechnologyDocument> toTechnologyDocuments(List<TechnologyInfo> technologies) {
        if (technologies == null) {
            return List.of();
        }

        return technologies.stream()
                .map(this::toTechnologyDocument)
                .collect(Collectors.toList());
    }

    private TechnologyDocument toTechnologyDocument(TechnologyInfo technology) {
        if (technology == null) {
            return null;
        }

        return new TechnologyDocument(
                technology.getId(),
                technology.getName(),
                technology.getDescription()
        );
    }

    private List<CapabilityWithTechnologies> toCapabilityDomains(List<CapabilityDocument> documents) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .map(this::toCapabilityDomain)
                .collect(Collectors.toList());
    }

    private CapabilityWithTechnologies toCapabilityDomain(CapabilityDocument document) {
        if (document == null) {
            return null;
        }

        CapabilityWithTechnologies capability = new CapabilityWithTechnologies();
        capability.setId(document.getId());
        capability.setName(document.getName());
        capability.setDescription(document.getDescription());
        capability.setTechnologies(toTechnologyDomains(document.getTechnologies()));

        return capability;
    }

    private List<TechnologyInfo> toTechnologyDomains(List<TechnologyDocument> documents) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .map(this::toTechnologyDomain)
                .collect(Collectors.toList());
    }

    private TechnologyInfo toTechnologyDomain(TechnologyDocument document) {
        if (document == null) {
            return null;
        }

        return new TechnologyInfo(
                document.getId(),
                document.getName(),
                document.getDescription()
        );
    }
}
