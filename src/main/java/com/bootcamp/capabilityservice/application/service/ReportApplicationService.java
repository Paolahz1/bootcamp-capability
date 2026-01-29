package com.bootcamp.capabilityservice.application.service;

import com.bootcamp.capabilityservice.application.dto.response.BootcampResponse;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.application.mapper.IBootcampMapper;
import com.bootcamp.capabilityservice.application.mapper.ICapabilityMapper;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import com.bootcamp.capabilityservice.domain.usecase.GetTopBootcampUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio de aplicación que coordina los casos de uso de reportes.
 */
@Service
public class ReportApplicationService {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final GetTopBootcampUseCase getTopBootcampUseCase;
    private final ITechnologyClientPort technologyClientPort;
    private final IBootcampMapper bootcampMapper;
    private final ICapabilityMapper capabilityMapper;

    public ReportApplicationService(
            GetTopBootcampUseCase getTopBootcampUseCase,
            ITechnologyClientPort technologyClientPort,
            IBootcampMapper bootcampMapper,
            ICapabilityMapper capabilityMapper) {
        this.getTopBootcampUseCase = getTopBootcampUseCase;
        this.technologyClientPort = technologyClientPort;
        this.bootcampMapper = bootcampMapper;
        this.capabilityMapper = capabilityMapper;
    }

    /**
     * Obtiene el bootcamp con más inscripciones.
     */
    public Mono<BootcampResponse> getTopBootcamp() {
        return getTopBootcampUseCase.execute()
            .flatMap(this::enrichBootcampWithCapabilities);
    }

    /**
     * Enriquece un bootcamp con datos completos de capacidades y tecnologías.
     */
    private Mono<BootcampResponse> enrichBootcampWithCapabilities(Bootcamp bootcamp) {
        if (bootcamp.getCapabilities() == null || bootcamp.getCapabilities().isEmpty()) {
            return Mono.just(bootcampMapper.toResponseWithCapabilities(bootcamp, List.of()));
        }

        return Flux.fromIterable(bootcamp.getCapabilities())
            .flatMap(this::enrichCapabilityWithTechnologies, DEFAULT_CONCURRENCY)
            .collectList()
            .map(capabilityResponses -> bootcampMapper.toResponseWithCapabilities(bootcamp, capabilityResponses));
    }

    /**
     * Enriquece una capacidad con datos completos de tecnologías.
     */
    private Mono<CapabilityResponse> enrichCapabilityWithTechnologies(Capability capability) {
        if (capability.getTechnologyIds() == null || capability.getTechnologyIds().isEmpty()) {
            return Mono.just(capabilityMapper.toResponseWithTechnologies(capability, List.of()));
        }

        return technologyClientPort.getTechnologiesByIds(capability.getTechnologyIds())
            .collectList()
            .map(technologies -> capabilityMapper.toResponseWithTechnologies(capability, technologies));
    }
}
