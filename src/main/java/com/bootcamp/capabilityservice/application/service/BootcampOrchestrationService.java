package com.bootcamp.capabilityservice.application.service;

import com.bootcamp.capabilityservice.application.dto.request.CreateBootcampRequest;
import com.bootcamp.capabilityservice.application.dto.request.EnrollmentRequest;
import com.bootcamp.capabilityservice.application.dto.response.BootcampResponse;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.application.dto.response.PageResponse;
import com.bootcamp.capabilityservice.application.mapper.IBootcampMapper;
import com.bootcamp.capabilityservice.application.mapper.ICapabilityMapper;
import com.bootcamp.capabilityservice.application.mapper.IPageMapper;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import com.bootcamp.capabilityservice.domain.usecase.CreateBootcampUseCase;
import com.bootcamp.capabilityservice.domain.usecase.DeleteBootcampSagaUseCase;
import com.bootcamp.capabilityservice.domain.usecase.EnrollPersonUseCase;
import com.bootcamp.capabilityservice.domain.usecase.ListBootcampsUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que coordina los casos de uso de orquestación de bootcamps.
 */
@Service
public class BootcampOrchestrationService {

    private static final int DEFAULT_CONCURRENCY = 5;

    private final CreateBootcampUseCase createBootcampUseCase;
    private final ListBootcampsUseCase listBootcampsUseCase;
    private final DeleteBootcampSagaUseCase deleteBootcampSagaUseCase;
    private final EnrollPersonUseCase enrollPersonUseCase;
    private final ITechnologyClientPort technologyClientPort;
    private final IBootcampMapper bootcampMapper;
    private final ICapabilityMapper capabilityMapper;
    private final IPageMapper pageMapper;

    public BootcampOrchestrationService(
            CreateBootcampUseCase createBootcampUseCase,
            ListBootcampsUseCase listBootcampsUseCase,
            DeleteBootcampSagaUseCase deleteBootcampSagaUseCase,
            EnrollPersonUseCase enrollPersonUseCase,
            ITechnologyClientPort technologyClientPort,
            IBootcampMapper bootcampMapper,
            ICapabilityMapper capabilityMapper,
            IPageMapper pageMapper) {
        this.createBootcampUseCase = createBootcampUseCase;
        this.listBootcampsUseCase = listBootcampsUseCase;
        this.deleteBootcampSagaUseCase = deleteBootcampSagaUseCase;
        this.enrollPersonUseCase = enrollPersonUseCase;
        this.technologyClientPort = technologyClientPort;
        this.bootcampMapper = bootcampMapper;
        this.capabilityMapper = capabilityMapper;
        this.pageMapper = pageMapper;
    }

    /**
     * Crea un nuevo bootcamp.
     */
    public Mono<BootcampResponse> createBootcamp(CreateBootcampRequest request) {
        Bootcamp bootcamp = bootcampMapper.toDomain(request);
        return createBootcampUseCase.execute(bootcamp, request.getCapabilityIds())
            .flatMap(this::enrichBootcampWithCapabilities);
    }

    /**
     * Lista bootcamps con paginación.
     */
    public Mono<PageResponse<BootcampResponse>> listBootcamps(int page, int size,
                                                               String sortBy, String direction) {
        return listBootcampsUseCase.execute(page, size, sortBy, direction)
            .flatMap(bootcampPage ->
                Flux.fromIterable(bootcampPage.getContent())
                    .flatMap(this::enrichBootcampWithCapabilities, DEFAULT_CONCURRENCY)
                    .collectList()
                    .map(enrichedList -> pageMapper.toResponse(bootcampPage, enrichedList))
            );
    }

    /**
     * Elimina un bootcamp usando Saga Pattern.
     */
    public Mono<Void> deleteBootcamp(Long bootcampId) {
        return deleteBootcampSagaUseCase.execute(bootcampId);
    }

    /**
     * Inscribe una persona en un bootcamp.
     */
    public Mono<Void> enrollPerson(EnrollmentRequest request) {
        return enrollPersonUseCase.execute(request.getBootcampId(), request.getPersonId());
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
