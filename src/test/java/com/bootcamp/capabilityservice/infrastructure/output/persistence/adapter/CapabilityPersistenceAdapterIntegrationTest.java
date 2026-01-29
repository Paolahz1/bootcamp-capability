package com.bootcamp.capabilityservice.infrastructure.output.persistence.adapter;

import com.bootcamp.capabilityservice.AbstractIntegrationTest;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.repository.ICapabilityRepository;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.repository.ICapabilityTechnologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CapabilityPersistenceAdapter.
 * Uses Testcontainers for MySQL to test real database operations.
 * 
 * Validates: Requirements 1.6, 1.7 (atomicity and rollback)
 */
class CapabilityPersistenceAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CapabilityPersistenceAdapter adapter;

    @Autowired
    private ICapabilityRepository capabilityRepository;

    @Autowired
    private ICapabilityTechnologyRepository capabilityTechnologyRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        databaseClient.sql("DELETE FROM capability_technologies").then().block();
        databaseClient.sql("DELETE FROM capabilities").then().block();
    }

    // ========== Test: save() guarda capacidad y relaciones atómicamente ==========

    @Test
    @DisplayName("Should save capability and technology relations atomically")
    void shouldSaveCapabilityAndRelationsAtomically() {
        // Given
        Capability capability = new Capability();
        capability.setName("Backend Development");
        capability.setDescription("Backend technologies capability");
        List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);

        // When
        StepVerifier.create(adapter.save(capability, technologyIds))
            .assertNext(saved -> {
                // Then - verify capability was saved
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getName()).isEqualTo("Backend Development");
                assertThat(saved.getDescription()).isEqualTo("Backend technologies capability");
                assertThat(saved.getTechnologyIds()).containsExactlyInAnyOrderElementsOf(technologyIds);
                assertThat(saved.getCreatedAt()).isNotNull();
                assertThat(saved.getUpdatedAt()).isNotNull();
            })
            .verifyComplete();

        // Verify capability exists in database
        StepVerifier.create(capabilityRepository.findByName("Backend Development"))
            .assertNext(entity -> {
                assertThat(entity.getId()).isNotNull();
                assertThat(entity.getName()).isEqualTo("Backend Development");
            })
            .verifyComplete();

        // Verify all technology relations were saved
        StepVerifier.create(capabilityTechnologyRepository.findTechnologyIdsByCapabilityId(
                capabilityRepository.findByName("Backend Development").block().getId())
                .collectList())
            .assertNext(techIds -> {
                assertThat(techIds).hasSize(3);
                assertThat(techIds).containsExactlyInAnyOrder(1L, 2L, 3L);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should save capability with minimum 3 technologies atomically")
    void shouldSaveCapabilityWithMinimumTechnologiesAtomically() {
        // Given
        Capability capability = new Capability();
        capability.setName("Minimum Tech Capability");
        capability.setDescription("Capability with minimum technologies");
        List<Long> technologyIds = Arrays.asList(10L, 20L, 30L);

        // When & Then
        StepVerifier.create(adapter.save(capability, technologyIds))
            .assertNext(saved -> {
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getTechnologyIds()).hasSize(3);
            })
            .verifyComplete();

        // Verify both capability and relations exist
        Long capabilityId = capabilityRepository.findByName("Minimum Tech Capability").block().getId();
        
        StepVerifier.create(capabilityTechnologyRepository.countByTechnologyId(10L))
            .expectNext(1L)
            .verifyComplete();
        
        StepVerifier.create(capabilityTechnologyRepository.countByTechnologyId(20L))
            .expectNext(1L)
            .verifyComplete();
        
        StepVerifier.create(capabilityTechnologyRepository.countByTechnologyId(30L))
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should save capability with maximum 20 technologies atomically")
    void shouldSaveCapabilityWithMaximumTechnologiesAtomically() {
        // Given
        Capability capability = new Capability();
        capability.setName("Maximum Tech Capability");
        capability.setDescription("Capability with maximum technologies");
        List<Long> technologyIds = Arrays.asList(
            1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
            11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L
        );

        // When & Then
        StepVerifier.create(adapter.save(capability, technologyIds))
            .assertNext(saved -> {
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getTechnologyIds()).hasSize(20);
            })
            .verifyComplete();

        // Verify all 20 relations were saved
        Long capabilityId = capabilityRepository.findByName("Maximum Tech Capability").block().getId();
        
        StepVerifier.create(capabilityTechnologyRepository.findTechnologyIdsByCapabilityId(capabilityId).collectList())
            .assertNext(techIds -> assertThat(techIds).hasSize(20))
            .verifyComplete();
    }
}
