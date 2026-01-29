package com.bootcamp.capabilityservice.infrastructure.output.client.adapter;

import com.bootcamp.capabilityservice.domain.exception.ExternalServiceException;
import com.bootcamp.capabilityservice.domain.model.Technology;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Tests unitarios para TechnologyClientAdapter usando WireMock.
 * Validates: Requirements 1.4, 2.2, 2.3, 4.2, 4.3
 */
class TechnologyClientAdapterTest {

    private WireMockServer wireMockServer;
    private TechnologyClientAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:" + wireMockServer.port())
            .build();

        adapter = new TechnologyClientAdapter(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    // ========== Tests for validateTechnologiesExist() ==========

    @Test
    @DisplayName("Should return true when all technologies exist")
    void shouldReturnTrueWhenAllTechnologiesExist() {
        // Given
        List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);

        stubFor(post(urlEqualTo("/api/technologies/validate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("true")));

        // When & Then
        StepVerifier.create(adapter.validateTechnologiesExist(technologyIds))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when some technologies do not exist")
    void shouldReturnFalseWhenSomeTechnologiesNotExist() {
        // Given
        List<Long> technologyIds = Arrays.asList(1L, 999L);

        stubFor(post(urlEqualTo("/api/technologies/validate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("false")));

        // When & Then
        StepVerifier.create(adapter.validateTechnologiesExist(technologyIds))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on server error during validation")
    void shouldThrowExceptionOnServerErrorDuringValidation() {
        // Given
        List<Long> technologyIds = Arrays.asList(1L, 2L);

        stubFor(post(urlEqualTo("/api/technologies/validate"))
            .willReturn(aResponse()
                .withStatus(500)));

        // When & Then
        StepVerifier.create(adapter.validateTechnologiesExist(technologyIds))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on client error during validation")
    void shouldThrowExceptionOnClientErrorDuringValidation() {
        // Given
        List<Long> technologyIds = Arrays.asList(1L, 2L);

        stubFor(post(urlEqualTo("/api/technologies/validate"))
            .willReturn(aResponse()
                .withStatus(400)));

        // When & Then
        StepVerifier.create(adapter.validateTechnologiesExist(technologyIds))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    // ========== Tests for getTechnologiesByIds() ==========

    @Test
    @DisplayName("Should get technologies by IDs successfully")
    void shouldGetTechnologiesByIdsSuccessfully() throws Exception {
        // Given
        List<Long> technologyIds = Arrays.asList(1L, 2L);

        String tech1Json = objectMapper.writeValueAsString(
            new TechnologyTestDto(1L, "Java", "Java programming language"));
        String tech2Json = objectMapper.writeValueAsString(
            new TechnologyTestDto(2L, "Spring", "Spring framework"));

        stubFor(get(urlEqualTo("/api/technologies/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(tech1Json)));

        stubFor(get(urlEqualTo("/api/technologies/2"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(tech2Json)));

        // When & Then
        StepVerifier.create(adapter.getTechnologiesByIds(technologyIds))
            .expectNextMatches(tech -> tech.getId().equals(1L) || tech.getId().equals(2L))
            .expectNextMatches(tech -> tech.getId().equals(1L) || tech.getId().equals(2L))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should skip non-existent technologies (404)")
    void shouldSkipNonExistentTechnologies() throws Exception {
        // Given
        List<Long> technologyIds = Arrays.asList(1L, 999L);

        String tech1Json = objectMapper.writeValueAsString(
            new TechnologyTestDto(1L, "Java", "Java programming language"));

        stubFor(get(urlEqualTo("/api/technologies/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(tech1Json)));

        stubFor(get(urlEqualTo("/api/technologies/999"))
            .willReturn(aResponse()
                .withStatus(404)));

        // When & Then
        StepVerifier.create(adapter.getTechnologiesByIds(technologyIds))
            .expectNextMatches(tech -> tech.getId().equals(1L) && tech.getName().equals("Java"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when all technologies not found")
    void shouldReturnEmptyFluxWhenAllTechnologiesNotFound() {
        // Given
        List<Long> technologyIds = Arrays.asList(998L, 999L);

        stubFor(get(urlEqualTo("/api/technologies/998"))
            .willReturn(aResponse()
                .withStatus(404)));

        stubFor(get(urlEqualTo("/api/technologies/999"))
            .willReturn(aResponse()
                .withStatus(404)));

        // When & Then
        StepVerifier.create(adapter.getTechnologiesByIds(technologyIds))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalServiceException on server error when getting technology")
    void shouldThrowExceptionOnServerErrorWhenGettingTechnology() {
        // Given
        List<Long> technologyIds = List.of(1L);

        stubFor(get(urlEqualTo("/api/technologies/1"))
            .willReturn(aResponse()
                .withStatus(500)));

        // When & Then
        StepVerifier.create(adapter.getTechnologiesByIds(technologyIds))
            .expectError(ExternalServiceException.class)
            .verify();
    }

    @Test
    @DisplayName("Should return empty flux for empty technology IDs list")
    void shouldReturnEmptyFluxForEmptyList() {
        // When & Then
        StepVerifier.create(adapter.getTechnologiesByIds(List.of()))
            .verifyComplete();
    }

    // Helper DTO for JSON serialization in tests
    private static class TechnologyTestDto {
        private Long id;
        private String name;
        private String description;

        public TechnologyTestDto() {}

        public TechnologyTestDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
