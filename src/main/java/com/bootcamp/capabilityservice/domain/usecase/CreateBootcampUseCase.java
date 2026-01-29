package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.exception.CapabilityNotFoundException;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.BootcampReport;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.CapabilityWithTechnologies;
import com.bootcamp.capabilityservice.domain.model.Technology;
import com.bootcamp.capabilityservice.domain.model.TechnologyInfo;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.IBootcampReportPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para crear bootcamps.
 * Valida que las capacidades existan antes de delegar la creación al Bootcamp Service.
 * Guarda un BootcampReport en MongoDB de forma asíncrona (fire-and-forget).
 */
public class CreateBootcampUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateBootcampUseCase.class);
    private static final int DEFAULT_CONCURRENCY = 5;

    private final ICapabilityPersistencePort capabilityPersistencePort;
    private final IBootcampClientPort bootcampClientPort;
    private final IBootcampReportPersistencePort reportPersistencePort;
    private final ITechnologyClientPort technologyClientPort;

    public CreateBootcampUseCase(ICapabilityPersistencePort capabilityPersistencePort,
                                 IBootcampClientPort bootcampClientPort,
                                 IBootcampReportPersistencePort reportPersistencePort,
                                 ITechnologyClientPort technologyClientPort) {
        this.capabilityPersistencePort = capabilityPersistencePort;
        this.bootcampClientPort = bootcampClientPort;
        this.reportPersistencePort = reportPersistencePort;
        this.technologyClientPort = technologyClientPort;
    }

    /**
     * Crea un nuevo bootcamp validando que las capacidades existan.
     * Guarda un BootcampReport en MongoDB de forma asíncrona sin bloquear la respuesta.
     *
     * @param bootcamp el bootcamp a crear
     * @param capabilityIds lista de IDs de capacidades
     * @return Mono con el bootcamp creado
     */
    public Mono<Bootcamp> execute(Bootcamp bootcamp, List<Long> capabilityIds) {
        return validateCapabilitiesExist(capabilityIds)
            .then(Mono.defer(() -> {
                bootcamp.setCapabilityIds(capabilityIds);
                return bootcampClientPort.createBootcamp(bootcamp);
            }))
            .flatMap(createdBootcamp -> {
                // Fire-and-forget: guarda en MongoDB sin bloquear
                saveBootcampReport(createdBootcamp, capabilityIds)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                        report -> log.debug("Report saved for bootcamp {}", createdBootcamp.getId()),
                        error -> log.error("Error saving report for bootcamp {}: {}", 
                            createdBootcamp.getId(), error.getMessage())
                    );
                
                return Mono.just(createdBootcamp);
            });
    }

    private Mono<Void> validateCapabilitiesExist(List<Long> capabilityIds) {
        if (capabilityIds == null || capabilityIds.isEmpty()) {
            return Mono.empty();
        }

        return capabilityPersistencePort.findByIds(capabilityIds)
            .collectList()
            .flatMap(foundCapabilities -> {
                if (foundCapabilities.size() != capabilityIds.size()) {
                    return Mono.error(new CapabilityNotFoundException());
                }
                return Mono.empty();
            });
    }

    private Mono<BootcampReport> saveBootcampReport(Bootcamp bootcamp, List<Long> capabilityIds) {
        return getCapabilitiesWithTechnologies(capabilityIds)
            .collectList()
            .flatMap(capabilities -> {
                int technologyCount = capabilities.stream()
                    .mapToInt(c -> c.getTechnologies().size())
                    .sum();

                BootcampReport report = new BootcampReport();
                report.setBootcampId(bootcamp.getId());
                report.setName(bootcamp.getName());
                report.setDescription(bootcamp.getDescription());
                report.setStartDate(bootcamp.getStartDate());
                report.setEndDate(bootcamp.getEndDate());
                report.setCapacityCount(capabilities.size());
                report.setTechnologyCount(technologyCount);
                report.setEnrollmentCount(0L);
                report.setCapabilities(capabilities);

                return reportPersistencePort.save(report);
            })
            .onErrorResume(e -> {
                log.error("Failed to save bootcamp report: {}", e.getMessage());
                return Mono.empty();
            });
    }

    private Flux<CapabilityWithTechnologies> getCapabilitiesWithTechnologies(List<Long> capabilityIds) {
        if (capabilityIds == null || capabilityIds.isEmpty()) {
            return Flux.empty();
        }

        return capabilityPersistencePort.findByIds(capabilityIds)
            .flatMap(this::enrichCapabilityWithTechnologies, DEFAULT_CONCURRENCY);
    }

    private Mono<CapabilityWithTechnologies> enrichCapabilityWithTechnologies(Capability capability) {
        return capabilityPersistencePort.findTechnologyIdsByCapabilityId(capability.getId())
            .collectList()
            .flatMap(technologyIds -> {
                if (technologyIds.isEmpty()) {
                    return Mono.just(toCapabilityWithTechnologies(capability, List.of()));
                }
                return technologyClientPort.getTechnologiesByIds(technologyIds)
                    .collectList()
                    .map(technologies -> toCapabilityWithTechnologies(capability, technologies));
            });
    }

    private CapabilityWithTechnologies toCapabilityWithTechnologies(Capability capability, List<Technology> technologies) {
        List<TechnologyInfo> techInfos = technologies.stream()
            .map(t -> new TechnologyInfo(t.getId(), t.getName(), t.getDescription()))
            .collect(Collectors.toList());

        return new CapabilityWithTechnologies(
            capability.getId(),
            capability.getName(),
            capability.getDescription(),
            techInfos
        );
    }
}
