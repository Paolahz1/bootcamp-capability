package com.bootcamp.capabilityservice.infrastructure.output.client.adapter;

import com.bootcamp.capabilityservice.domain.exception.ExternalServiceException;
import com.bootcamp.capabilityservice.domain.model.Technology;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import com.bootcamp.capabilityservice.infrastructure.output.client.dto.TechnologyDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Adaptador cliente para comunicación con Technology Service.
 */
@Component
public class TechnologyClientAdapter implements ITechnologyClientPort {

    private static final int DEFAULT_CONCURRENCY = 5;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final String SERVICE_NAME = "Technology Service";

    private final WebClient webClient;

    public TechnologyClientAdapter(@Qualifier("technologyWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Boolean> validateTechnologiesExist(List<Long> technologyIds) {
        return webClient
            .post()
            .uri("/api/technologies/validate")
            .bodyValue(technologyIds)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new ExternalServiceException(SERVICE_NAME, "Validation failed")))
            .bodyToMono(Boolean.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(TimeoutException.class, e ->
                new ExternalServiceException(SERVICE_NAME, "Timeout"))
            .onErrorResume(e -> {
                if (e instanceof ExternalServiceException) {
                    return Mono.error(e);
                }
                return Mono.error(new ExternalServiceException(SERVICE_NAME, e.getMessage()));
            });
    }

    @Override
    public Flux<Technology> getTechnologiesByIds(List<Long> technologyIds) {
        return Flux.fromIterable(technologyIds)
            .flatMap(this::getTechnologyById, DEFAULT_CONCURRENCY);
    }

    private Mono<Technology> getTechnologyById(Long id) {
        return webClient
            .get()
            .uri("/api/technologies/{id}", id)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                Mono.empty())
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                Mono.error(new ExternalServiceException(SERVICE_NAME)))
            .bodyToMono(TechnologyDto.class)
            .timeout(DEFAULT_TIMEOUT)
            .map(this::toTechnology)
            .onErrorResume(e -> {
                if (e instanceof ExternalServiceException) {
                    return Mono.error(e);
                }
                return Mono.empty();
            });
    }

    private Technology toTechnology(TechnologyDto dto) {
        return new Technology(dto.getId(), dto.getName(), dto.getDescription());
    }
}
