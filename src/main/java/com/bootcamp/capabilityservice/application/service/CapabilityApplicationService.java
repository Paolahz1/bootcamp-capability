package com.bootcamp.capabilityservice.application.service;

import com.bootcamp.capabilityservice.application.dto.request.CreateCapabilityRequest;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.application.dto.response.PageResponse;
import com.bootcamp.capabilityservice.application.mapper.ICapabilityMapper;
import com.bootcamp.capabilityservice.application.mapper.IPageMapper;
import com.bootcamp.capabilityservice.domain.api.ICapabilityServicePort;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import com.bootcamp.capabilityservice.domain.usecase.CountCapabilitiesByTechnologyUseCase;
import com.bootcamp.capabilityservice.domain.usecase.CreateCapabilityUseCase;
import com.bootcamp.capabilityservice.domain.usecase.DeleteCapabilityUseCase;
import com.bootcamp.capabilityservice.domain.usecase.GetCapabilitiesByIdsUseCase;
import com.bootcamp.capabilityservice.domain.usecase.GetCapabilityByIdUseCase;
import com.bootcamp.capabilityservice.domain.usecase.ListCapabilitiesUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio de aplicación que coordina los casos de uso de capacidades.
 * Maneja la conversión entre DTOs y modelos de dominio.
 */
@Service
public class CapabilityApplicationService {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final CreateCapabilityUseCase createCapabilityUseCase;
    private final ListCapabilitiesUseCase listCapabilitiesUseCase;
    private final GetCapabilityByIdUseCase getCapabilityByIdUseCase;
    private final GetCapabilitiesByIdsUseCase getCapabilitiesByIdsUseCase;
    private final CountCapabilitiesByTechnologyUseCase countCapabilitiesByTechnologyUseCase;
    private final DeleteCapabilityUseCase deleteCapabilityUseCase;
    private final ITechnologyClientPort technologyClientPort;
    private final ICapabilityMapper capabilityMapper;
    private final IPageMapper pageMapper;

    public CapabilityApplicationService(
            CreateCapabilityUseCase createCapabilityUseCase,
            ListCapabilitiesUseCase listCapabilitiesUseCase,
            GetCapabilityByIdUseCase getCapabilityByIdUseCase,
            GetCapabilitiesByIdsUseCase getCapabilitiesByIdsUseCase,
            CountCapabilitiesByTechnologyUseCase countCapabilitiesByTechnologyUseCase,
            DeleteCapabilityUseCase deleteCapabilityUseCase,
            ITechnologyClientPort technologyClientPort,
            ICapabilityMapper capabilityMapper,
            IPageMapper pageMapper) {
        this.createCapabilityUseCase = createCapabilityUseCase;
        this.listCapabilitiesUseCase = listCapabilitiesUseCase;
        this.getCapabilityByIdUseCase = getCapabilityByIdUseCase;
        this.getCapabilitiesByIdsUseCase = getCapabilitiesByIdsUseCase;
        this.countCapabilitiesByTechnologyUseCase = countCapabilitiesByTechnologyUseCase;
        this.deleteCapabilityUseCase = deleteCapabilityUseCase;
        this.technologyClientPort = technologyClientPort;
        this.capabilityMapper = capabilityMapper;
        this.pageMapper = pageMapper;
    }

    /**
     * Crea una nueva capacidad.
     */
    public Mono<CapabilityResponse> createCapability(CreateCapabilityRequest request) {
        Capability capability = capabilityMapper.toDomain(request);
        return createCapabilityUseCase.execute(capability, request.getTechnologyIds())
            .flatMap(this::enrichWithTechnologies);
    }

    /**
     * Lista capacidades con paginación.
     */
    public Mono<PageResponse<CapabilityResponse>> listCapabilities(int page, int size, 
                                                                    String sortBy, String direction) {
        return listCapabilitiesUseCase.execute(page, size, sortBy, direction)
            .flatMap(capabilityPage -> 
                Flux.fromIterable(capabilityPage.getContent())
                    .flatMap(this::enrichWithTechnologies, DEFAULT_CONCURRENCY)
                    .collectList()
                    .map(enrichedList -> pageMapper.toResponse(capabilityPage, enrichedList))
            );
    }

    /**
     * Obtiene una capacidad por ID.
     */
    public Mono<CapabilityResponse> getCapabilityById(Long id) {
        return getCapabilityByIdUseCase.execute(id)
            .flatMap(this::enrichWithTechnologies);
    }

    /**
     * Obtiene múltiples capacidades por IDs.
     */
    public Flux<CapabilityResponse> getCapabilitiesByIds(List<Long> ids) {
        return getCapabilitiesByIdsUseCase.execute(ids)
            .flatMap(this::enrichWithTechnologies, DEFAULT_CONCURRENCY);
    }

    /**
     * Cuenta capacidades por tecnología.
     */
    public Mono<Long> countCapabilitiesByTechnology(Long technologyId) {
        return countCapabilitiesByTechnologyUseCase.execute(technologyId);
    }

    /**
     * Elimina una capacidad.
     */
    public Mono<Void> deleteCapability(Long id) {
        return deleteCapabilityUseCase.execute(id);
    }

    /**
     * Enriquece una capacidad con datos completos de tecnologías.
     */
    private Mono<CapabilityResponse> enrichWithTechnologies(Capability capability) {
        if (capability.getTechnologyIds() == null || capability.getTechnologyIds().isEmpty()) {
            return Mono.just(capabilityMapper.toResponseWithTechnologies(capability, List.of()));
        }

        return technologyClientPort.getTechnologiesByIds(capability.getTechnologyIds())
            .collectList()
            .map(technologies -> capabilityMapper.toResponseWithTechnologies(capability, technologies));
    }
}
