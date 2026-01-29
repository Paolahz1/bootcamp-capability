package com.bootcamp.capabilityservice.infrastructure.input.rest.handler;

import com.bootcamp.capabilityservice.application.dto.request.CreateBootcampRequest;
import com.bootcamp.capabilityservice.application.dto.request.EnrollmentRequest;
import com.bootcamp.capabilityservice.application.service.BootcampOrchestrationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler para endpoints de bootcamps (orquestación).
 */
@Component
public class BootcampHandler {

    private final BootcampOrchestrationService orchestrationService;
    private final Validator validator;

    public BootcampHandler(BootcampOrchestrationService orchestrationService, Validator validator) {
        this.orchestrationService = orchestrationService;
        this.validator = validator;
    }

    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        return request.bodyToMono(CreateBootcampRequest.class)
            .flatMap(this::validate)
            .flatMap(orchestrationService::createBootcamp)
            .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public Mono<ServerResponse> listBootcamps(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String direction = request.queryParam("direction").orElse("ASC");

        return orchestrationService.listBootcamps(page, size, sortBy, direction)
            .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return orchestrationService.deleteBootcamp(id)
            .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> enrollPerson(ServerRequest request) {
        return request.bodyToMono(EnrollmentRequest.class)
            .flatMap(this::validate)
            .flatMap(orchestrationService::enrollPerson)
            .then(ServerResponse.status(HttpStatus.CREATED).build());
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
}
