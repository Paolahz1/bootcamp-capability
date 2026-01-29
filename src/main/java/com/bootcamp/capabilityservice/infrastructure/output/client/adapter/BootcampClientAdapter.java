package com.bootcamp.capabilityservice.infrastructure.output.client.adapter;

import com.bootcamp.capabilityservice.domain.exception.ExternalServiceException;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.infrastructure.output.client.dto.BootcampDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Adaptador cliente para comunicación con Bootcamp Service.
 */
@Component
public class BootcampClientAdapter implements IBootcampClientPort {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final String SERVICE_NAME = "Bootcamp Service";

    private final WebClient webClient;

    public BootcampClientAdapter(@Qualifier("bootcampWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Long> countBootcampsByCapabilityId(Long capabilityId) {
        return webClient.get()
            .uri("/api/bootcamps/count-by-capability/{capabilityId}", capabilityId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToMono(Long.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }


    @Override
    public Mono<Bootcamp> createBootcamp(Bootcamp bootcamp) {
        BootcampDto dto = toDto(bootcamp);
        return webClient.post()
            .uri("/api/bootcamps")
            .bodyValue(dto)
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToMono(BootcampDto.class)
            .timeout(DEFAULT_TIMEOUT)
            .map(this::toBootcamp)
            .onErrorMap(this::mapError);
    }

    @Override
    public Mono<Page<Bootcamp>> listBootcamps(int page, int size, String sortBy, String direction) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/bootcamps")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .queryParam("direction", direction)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .timeout(DEFAULT_TIMEOUT)
            .map(this::mapToPage)
            .onErrorMap(this::mapError);
    }

    @Override
    public Mono<Void> deleteBootcamp(Long bootcampId) {
        return webClient.delete()
            .uri("/api/bootcamps/{id}", bootcampId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToMono(Void.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }

    @Override
    public Mono<Bootcamp> getTopBootcamp() {
        return webClient.get()
            .uri("/api/bootcamps/top")
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToMono(BootcampDto.class)
            .timeout(DEFAULT_TIMEOUT)
            .map(this::toBootcamp)
            .onErrorMap(this::mapError);
    }


    @Override
    public Flux<Long> getCapabilityIdsByBootcampId(Long bootcampId) {
        return webClient.get()
            .uri("/api/bootcamps/{id}/capabilities", bootcampId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToFlux(Long.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }

    @Override
    public Mono<Void> enrollPerson(Long bootcampId, Long personId) {
        return webClient.post()
            .uri("/api/bootcamps/{bootcampId}/enrollments", bootcampId)
            .bodyValue(Map.of("personId", personId))
            .retrieve()
            .onStatus(HttpStatusCode::isError, r -> handleError())
            .bodyToMono(Void.class)
            .timeout(DEFAULT_TIMEOUT)
            .onErrorMap(this::mapError);
    }

    private Mono<Throwable> handleError() {
        return Mono.error(new ExternalServiceException(SERVICE_NAME));
    }

    private Throwable mapError(Throwable e) {
        if (e instanceof ExternalServiceException) return e;
        if (e instanceof TimeoutException) return new ExternalServiceException(SERVICE_NAME, "Timeout");
        return new ExternalServiceException(SERVICE_NAME, e.getMessage());
    }

    private BootcampDto toDto(Bootcamp bootcamp) {
        return new BootcampDto(bootcamp.getId(), bootcamp.getName(), bootcamp.getDescription(),
            bootcamp.getStartDate(), bootcamp.getEndDate(), bootcamp.getCapabilityIds());
    }

    private Bootcamp toBootcamp(BootcampDto dto) {
        return new Bootcamp(dto.getId(), dto.getName(), dto.getDescription(),
            dto.getStartDate(), dto.getEndDate(), dto.getCapabilityIds());
    }

    @SuppressWarnings("unchecked")
    private Page<Bootcamp> mapToPage(Map<String, Object> map) {
        List<Map<String, Object>> content = (List<Map<String, Object>>) map.get("content");
        List<Bootcamp> bootcamps = content.stream().map(this::mapToBootcamp).toList();
        int pageNumber = (Integer) map.getOrDefault("pageNumber", 0);
        int pageSize = (Integer) map.getOrDefault("pageSize", 10);
        long totalElements = ((Number) map.getOrDefault("totalElements", 0L)).longValue();
        return new Page<>(bootcamps, pageNumber, pageSize, totalElements);
    }

    @SuppressWarnings("unchecked")
    private Bootcamp mapToBootcamp(Map<String, Object> map) {
        return new Bootcamp(
            ((Number) map.get("id")).longValue(),
            (String) map.get("name"),
            (String) map.get("description"),
            null, null,
            (List<Long>) map.get("capabilityIds")
        );
    }
}
