package com.bootcamp.capabilityservice.infrastructure.input.rest.handler;

import com.bootcamp.capabilityservice.application.dto.request.CreateCapabilityRequest;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.application.dto.response.PageResponse;
import com.bootcamp.capabilityservice.application.service.CapabilityApplicationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler para endpoints de capacidades.
 */
@Component
public class CapabilityHandler {

    private final CapabilityApplicationService applicationService;
    private final Validator validator;

    public CapabilityHandler(CapabilityApplicationService applicationService, Validator validator) {
        this.applicationService = applicationService;
        this.validator = validator;
    }

    public Mono<ServerResponse> createCapability(ServerRequest request) {
        return request.bodyToMono(CreateCapabilityRequest.class)
            .flatMap(this::validate)
            .flatMap(applicationService::createCapability)
            .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    private <T> Mono<T> validate(T obj) {
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        if (violations.isEmpty()) {
            return Mono.just(obj);
        }
        String errorMessage = violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        return Mono.error(new ServerWebInputException(errorMessage));
    }

    public Mono<ServerResponse> listCapabilities(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String direction = request.queryParam("direction").orElse("ASC");

        return applicationService.listCapabilities(page, size, sortBy, direction)
            .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }


    public Mono<ServerResponse> getCapabilityById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return applicationService.getCapabilityById(id)
            .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public Mono<ServerResponse> getCapabilitiesByIds(ServerRequest request) {
        String idsParam = request.queryParam("ids").orElse("");
        List<Long> ids = Arrays.stream(idsParam.split(","))
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .toList();

        return applicationService.getCapabilitiesByIds(ids)
            .collectList()
            .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public Mono<ServerResponse> countByTechnology(ServerRequest request) {
        Long technologyId = Long.parseLong(request.pathVariable("technologyId"));
        return applicationService.countCapabilitiesByTechnology(technologyId)
            .flatMap(count -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(count));
    }

    public Mono<ServerResponse> deleteCapability(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return applicationService.deleteCapability(id)
            .then(ServerResponse.noContent().build());
    }
}
