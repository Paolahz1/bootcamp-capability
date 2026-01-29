package com.bootcamp.capabilityservice.infrastructure.output.client.adapter;

import com.bootcamp.capabilityservice.domain.exception.ExternalServiceException;
import com.bootcamp.capabilityservice.domain.model.PersonInfo;
import com.bootcamp.capabilityservice.domain.spi.IPersonClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Adaptador cliente para comunicación con Person Service.
 */
@Component
public class PersonClientAdapter implements IPersonClientPort {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final String SERVICE_NAME = "Person Service";

    private final WebClient webClient;

    public PersonClientAdapter(@Qualifier("personWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> deleteEnrollmentsByBootcampId(Long bootcampId) {
        return webClient.delete()
            .uri("/api/enrollments/bootcamp/{bootcampId}", bootcampId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new ExternalServiceException(SERVICE_NAME)))
            .bodyToMono(Void.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }

    @Override
    public Mono<Void> enrollPerson(Long bootcampId, Long personId) {
        return webClient.post()
            .uri("/api/enrollments")
            .bodyValue(Map.of("bootcampId", bootcampId, "personId", personId))
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new ExternalServiceException(SERVICE_NAME)))
            .bodyToMono(Void.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }

    @Override
    public Flux<PersonInfo> getEnrolleesByBootcampId(Long bootcampId) {
        return webClient.get()
            .uri("/api/enrollments/bootcamp/{bootcampId}/persons", bootcampId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new ExternalServiceException(SERVICE_NAME)))
            .bodyToFlux(PersonInfo.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }

    private Throwable mapError(Throwable e) {
        if (e instanceof ExternalServiceException) return e;
        if (e instanceof TimeoutException) return new ExternalServiceException(SERVICE_NAME, "Timeout");
        return new ExternalServiceException(SERVICE_NAME, e.getMessage());
    }
}
