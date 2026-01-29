package com.bootcamp.capabilityservice.infrastructure.input.rest;

import com.bootcamp.capabilityservice.AbstractIntegrationTest;
import com.bootcamp.capabilityservice.application.dto.request.CreateCapabilityRequest;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.application.dto.response.ErrorResponse;
import com.bootcamp.capabilityservice.application.dto.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Integration tests for Capability Router endpoints.
 * Uses Testcontainers for MySQL and WireMock for external services.
 * 
 * Validates: Requirements 1, 2, 3, 6
 */
class CapabilityRouterIntegrationTest extends AbstractIntegrationTest {

    private static WireMockServer technologyWireMock;
    private static WireMockServer bootcampWireMock;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUpWireMock() {
        technologyWireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        technologyWireMock.start();

        bootcampWireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        bootcampWireMock.start();
    }

    @AfterAll
    static void tearDownWireMock() {
        if (technologyWireMock != null) {
            technologyWireMock.stop();
        }
        if (bootcampWireMock != null) {
            bootcampWireMock.stop();
        }
    }

    @DynamicPropertySource
    static void configureExternalServices(DynamicPropertyRegistry registry) {
        registry.add("external-services.technology.base-url", 
            () -> "http://localhost:" + technologyWireMock.port());
        registry.add("external-services.bootcamp.base-url", 
            () -> "http://localhost:" + bootcampWireMock.port());
    }

    @BeforeEach
    void setUp() {
        technologyWireMock.resetAll();
        bootcampWireMock.resetAll();
        cleanDatabase();
    }

    private void cleanDatabase() {
        databaseClient.sql("DELETE FROM capability_technologies").then().block();
        databaseClient.sql("DELETE FROM capabilities").then().block();
    }

    // ========== Helper Methods ==========

    private void stubTechnologyValidation(boolean result) {
        technologyWireMock.stubFor(post(urlEqualTo("/api/technologies/validate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(String.valueOf(result))));
    }

    private void stubTechnologyById(Long id, String name, String description) throws Exception {
        String json = String.format(
            "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\"}", 
            id, name, description);
        
        technologyWireMock.stubFor(get(urlEqualTo("/api/technologies/" + id))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(json)));
    }

    private void stubBootcampCountByCapability(Long capabilityId, long count) {
        bootcampWireMock.stubFor(get(urlEqualTo("/api/bootcamps/count-by-capability/" + capabilityId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(String.valueOf(count))));
    }

    private CreateCapabilityRequest createValidRequest() {
        return new CreateCapabilityRequest(
            "Backend Development",
            "Backend technologies capability",
            Arrays.asList(1L, 2L, 3L)
        );
    }

    private void insertCapabilityDirectly(Long id, String name, String description) {
        databaseClient.sql("INSERT INTO capabilities (id, name, description) VALUES (:id, :name, :description)")
            .bind("id", id)
            .bind("name", name)
            .bind("description", description)
            .then()
            .block();
    }

    private void insertCapabilityTechnology(Long capabilityId, Long technologyId) {
        databaseClient.sql("INSERT INTO capability_technologies (capability_id, technology_id) VALUES (:capId, :techId)")
            .bind("capId", capabilityId)
            .bind("techId", technologyId)
            .then()
            .block();
    }

    // ========== Test: Verify Integration Test Setup ==========

    @Test
    @DisplayName("Should verify integration test setup is working")
    void shouldVerifyIntegrationTestSetup() {
        // Verify WebTestClient is available
        Assertions.assertNotNull(webTestClient, "WebTestClient should be autowired");
        
        // Verify DatabaseClient is available
        Assertions.assertNotNull(databaseClient, "DatabaseClient should be autowired");
        
        // Verify WireMock servers are running
        Assertions.assertTrue(technologyWireMock.isRunning(), "Technology WireMock should be running");
        Assertions.assertTrue(bootcampWireMock.isRunning(), "Bootcamp WireMock should be running");
    }
}
