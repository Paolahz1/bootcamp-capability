package com.bootcamp.capabilityservice.infrastructure.output.client.adapter;

import com.bootcamp.capabilityservice.domain.exception.ExternalServiceException;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Tests unitarios para BootcampClientAdapter usando WireMock.
 * Validates: Requirements 6.3, 6.4, 7.3, 7.4, 8.1, 8.2, 8.3, 8.4, 9.2, 9.3, 9.4, 9.5, 9.6, 10.1, 11.1, 11.2, 11.3
 */
class BootcampClientAdapterTest {

    private WireMockServer wireMockServer;
    private BootcampClientAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:" + wireMockServer.port())
            .build();

        adapter = new BootcampClientAdapter(webClient);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    // ========== Tests for countBootcampsByCapabilityId() ==========

    @Test
    @DisplayName("Should count bootcamps by capability ID")
    void shouldCountBootcampsByCapabilityId() {
        // Given
        Long capabilityId = 1L;

        stubFor(get(urlEqualTo("/api/bootcamps/count-by-capability/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("5")));

        // When & Then
        StepVerifier.create(adapter.countBootcampsByCapabilityId(capabilityId))
            .expectNext(5L)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return zero when no bootcamps use capability")
    void shouldReturnZeroWhenNoBootcampsUseCapability() {
        // Given
        Long capabilityId = 999L;

        stubFor(get(urlEqualTo("/api/bootcamps/count-by-capability/999"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("0")));

        // When & Then
        StepVerifier.create(adapter.countBootcampsByCapabilityId(capabilityId))
            .expectNext(0L)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on error counting bootcamps")
    void shouldThrowExceptionOnErrorCountingBootcamps() {
        // Given
        Long capabilityId = 1L;

        stubFor(get(urlEqualTo("/api/bootcamps/count-by-capability/1"))
            .willReturn(aResponse()
                .withStatus(500)));

        // When & Then
        StepVerifier.create(adapter.countBootcampsByCapabilityId(capabilityId))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    // ========== Tests for createBootcamp() ==========

    @Test
    @DisplayName("Should create bootcamp successfully")
    void shouldCreateBootcampSuccessfully() throws Exception {
        // Given
        Bootcamp bootcamp = new Bootcamp(null, "Full Stack", "Full stack bootcamp",
            LocalDate.of(2024, 3, 1), LocalDate.of(2024, 6, 1), Arrays.asList(1L, 2L));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", 1);
        responseBody.put("name", "Full Stack");
        responseBody.put("description", "Full stack bootcamp");
        responseBody.put("startDate", "2024-03-01");
        responseBody.put("endDate", "2024-06-01");
        responseBody.put("capabilityIds", Arrays.asList(1, 2));

        stubFor(post(urlEqualTo("/api/bootcamps"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(responseBody))));

        // When & Then
        StepVerifier.create(adapter.createBootcamp(bootcamp))
            .expectNextMatches(result -> 
                result.getId().equals(1L) && 
                result.getName().equals("Full Stack"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on error creating bootcamp")
    void shouldThrowExceptionOnErrorCreatingBootcamp() {
        // Given
        Bootcamp bootcamp = new Bootcamp(null, "Test", "Test bootcamp",
            LocalDate.now(), LocalDate.now().plusMonths(3), List.of(1L));

        stubFor(post(urlEqualTo("/api/bootcamps"))
            .willReturn(aResponse()
                .withStatus(400)));

        // When & Then
        StepVerifier.create(adapter.createBootcamp(bootcamp))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    // ========== Tests for listBootcamps() ==========

    @Test
    @DisplayName("Should list bootcamps with pagination")
    void shouldListBootcampsWithPagination() throws Exception {
        // Given
        Map<String, Object> pageResponse = new HashMap<>();
        List<Map<String, Object>> content = Arrays.asList(
            createBootcampMap(1L, "Bootcamp 1", "Description 1", Arrays.asList(1L, 2L)),
            createBootcampMap(2L, "Bootcamp 2", "Description 2", Arrays.asList(3L))
        );
        pageResponse.put("content", content);
        pageResponse.put("pageNumber", 0);
        pageResponse.put("pageSize", 10);
        pageResponse.put("totalElements", 2);

        stubFor(get(urlPathEqualTo("/api/bootcamps"))
            .withQueryParam("page", equalTo("0"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("sortBy", equalTo("name"))
            .withQueryParam("direction", equalTo("ASC"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(pageResponse))));

        // When & Then
        StepVerifier.create(adapter.listBootcamps(0, 10, "name", "ASC"))
            .expectNextMatches(page -> 
                page.getContent().size() == 2 &&
                page.getPageNumber() == 0 &&
                page.getTotalElements() == 2)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty page when no bootcamps exist")
    void shouldReturnEmptyPageWhenNoBootcamps() throws Exception {
        // Given
        Map<String, Object> pageResponse = new HashMap<>();
        pageResponse.put("content", List.of());
        pageResponse.put("pageNumber", 0);
        pageResponse.put("pageSize", 10);
        pageResponse.put("totalElements", 0);

        stubFor(get(urlPathEqualTo("/api/bootcamps"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(pageResponse))));

        // When & Then
        StepVerifier.create(adapter.listBootcamps(0, 10, "name", "ASC"))
            .expectNextMatches(page -> 
                page.getContent().isEmpty() &&
                page.getTotalElements() == 0)
            .verifyComplete();
    }

    // ========== Tests for deleteBootcamp() ==========

    @Test
    @DisplayName("Should delete bootcamp successfully")
    void shouldDeleteBootcampSuccessfully() {
        // Given
        Long bootcampId = 1L;

        stubFor(delete(urlEqualTo("/api/bootcamps/1"))
            .willReturn(aResponse()
                .withStatus(204)));

        // When & Then
        StepVerifier.create(adapter.deleteBootcamp(bootcampId))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on error deleting bootcamp")
    void shouldThrowExceptionOnErrorDeletingBootcamp() {
        // Given
        Long bootcampId = 1L;

        stubFor(delete(urlEqualTo("/api/bootcamps/1"))
            .willReturn(aResponse()
                .withStatus(500)));

        // When & Then
        StepVerifier.create(adapter.deleteBootcamp(bootcampId))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    // ========== Tests for getTopBootcamp() ==========

    @Test
    @DisplayName("Should get top bootcamp successfully")
    void shouldGetTopBootcampSuccessfully() throws Exception {
        // Given
        Map<String, Object> bootcampResponse = new HashMap<>();
        bootcampResponse.put("id", 1);
        bootcampResponse.put("name", "Popular Bootcamp");
        bootcampResponse.put("description", "Most enrolled bootcamp");
        bootcampResponse.put("startDate", "2024-03-01");
        bootcampResponse.put("endDate", "2024-06-01");
        bootcampResponse.put("capabilityIds", Arrays.asList(1, 2, 3));

        stubFor(get(urlEqualTo("/api/bootcamps/top"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(bootcampResponse))));

        // When & Then
        StepVerifier.create(adapter.getTopBootcamp())
            .expectNextMatches(bootcamp -> 
                bootcamp.getId().equals(1L) && 
                bootcamp.getName().equals("Popular Bootcamp"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException when no top bootcamp found")
    void shouldThrowExceptionWhenNoTopBootcampFound() {
        // Given
        stubFor(get(urlEqualTo("/api/bootcamps/top"))
            .willReturn(aResponse()
                .withStatus(404)));

        // When & Then
        StepVerifier.create(adapter.getTopBootcamp())
            .expectError(ExternalServiceException.class)
            .verify();
    }

    // ========== Tests for getCapabilityIdsByBootcampId() ==========

    @Test
    @DisplayName("Should get capability IDs by bootcamp ID")
    void shouldGetCapabilityIdsByBootcampId() {
        // Given
        Long bootcampId = 1L;

        stubFor(get(urlEqualTo("/api/bootcamps/1/capabilities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("[1, 2, 3]")));

        // When & Then
        StepVerifier.create(adapter.getCapabilityIdsByBootcampId(bootcampId))
            .expectNext(1L)
            .expectNext(2L)
            .expectNext(3L)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when bootcamp has no capabilities")
    void shouldReturnEmptyFluxWhenBootcampHasNoCapabilities() {
        // Given
        Long bootcampId = 1L;

        stubFor(get(urlEqualTo("/api/bootcamps/1/capabilities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("[]")));

        // When & Then
        StepVerifier.create(adapter.getCapabilityIdsByBootcampId(bootcampId))
            .verifyComplete();
    }

    // ========== Tests for enrollPerson() ==========

    @Test
    @DisplayName("Should enroll person successfully")
    void shouldEnrollPersonSuccessfully() {
        // Given
        Long bootcampId = 1L;
        Long personId = 100L;

        stubFor(post(urlEqualTo("/api/bootcamps/1/enrollments"))
            .willReturn(aResponse()
                .withStatus(201)));

        // When & Then
        StepVerifier.create(adapter.enrollPerson(bootcampId, personId))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on error enrolling person")
    void shouldThrowExceptionOnErrorEnrollingPerson() {
        // Given
        Long bootcampId = 1L;
        Long personId = 100L;

        stubFor(post(urlEqualTo("/api/bootcamps/1/enrollments"))
            .willReturn(aResponse()
                .withStatus(400)));

        // When & Then
        StepVerifier.create(adapter.enrollPerson(bootcampId, personId))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    // Helper method to create bootcamp map for JSON serialization
    private Map<String, Object> createBootcampMap(Long id, String name, String description, List<Long> capabilityIds) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("capabilityIds", capabilityIds);
        return map;
    }
}
