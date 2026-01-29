package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.exception.CapabilityNotFoundException;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Tests unitarios para GetCapabilityByIdUseCase.
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5
 */
@ExtendWith(MockitoExtension.class)
class GetCapabilityByIdUseCaseTest {

    @Mock
    private ICapabilityPersistencePort persistencePort;

    @Mock
    private ITechnologyClientPort technologyClientPort;

    private GetCapabilityByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCapabilityByIdUseCase(persistencePort, technologyClientPort);
    }

    private Capability createCapability(Long id, String name, List<Long> techIds) {
        return new Capability(id, name, "Description for " + name, techIds, 
            LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return capability when it exists")
    void shouldReturnCapabilityWhenExists() {
        // Given
        Long capabilityId = 1L;
        List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);
        Capability capability = createCapability(capabilityId, "Backend Development", null);

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(persistencePort.findTechnologyIdsByCapabilityId(capabilityId)).thenReturn(Flux.fromIterable(technologyIds));

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectNextMatches(result -> 
                result.getId().equals(capabilityId) &&
                result.getName().equals("Backend Development") &&
                result.getTechnologyIds().size() == 3)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw CapabilityNotFoundException when capability does not exist")
    void shouldThrowNotFoundExceptionWhenCapabilityNotExists() {
        // Given
        Long nonExistentId = 999L;

        when(persistencePort.findById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(nonExistentId))
            .expectErrorMatches(error -> 
                error instanceof CapabilityNotFoundException &&
                ((CapabilityNotFoundException) error).getAdditionalInfo().get("details").contains("999"))
            .verify();
    }

    @Test
    @DisplayName("Should load technology IDs for existing capability")
    void shouldLoadTechnologyIdsForExistingCapability() {
        // Given
        Long capabilityId = 2L;
        List<Long> technologyIds = Arrays.asList(10L, 20L, 30L, 40L, 50L);
        Capability capability = createCapability(capabilityId, "Full Stack", null);

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(persistencePort.findTechnologyIdsByCapabilityId(capabilityId)).thenReturn(Flux.fromIterable(technologyIds));

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectNextMatches(result -> 
                result.getTechnologyIds().containsAll(technologyIds) &&
                result.getTechnologyIds().size() == 5)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle capability with no associated technologies")
    void shouldHandleCapabilityWithNoTechnologies() {
        // Given
        Long capabilityId = 3L;
        Capability capability = createCapability(capabilityId, "Empty Capability", null);

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(persistencePort.findTechnologyIdsByCapabilityId(capabilityId)).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectNextMatches(result -> 
                result.getId().equals(capabilityId) &&
                result.getTechnologyIds().isEmpty())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return capability with all fields populated")
    void shouldReturnCapabilityWithAllFields() {
        // Given
        Long capabilityId = 4L;
        LocalDateTime now = LocalDateTime.now();
        Capability capability = new Capability(capabilityId, "Data Science", "Data science description", 
            null, now, now);
        List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(persistencePort.findTechnologyIdsByCapabilityId(capabilityId)).thenReturn(Flux.fromIterable(technologyIds));

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectNextMatches(result -> 
                result.getId().equals(capabilityId) &&
                result.getName().equals("Data Science") &&
                result.getDescription().equals("Data science description") &&
                result.getCreatedAt() != null &&
                result.getUpdatedAt() != null)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle different capability IDs correctly")
    void shouldHandleDifferentCapabilityIds() {
        // Given
        Long capabilityId = 100L;
        Capability capability = createCapability(capabilityId, "Mobile Development", null);
        List<Long> technologyIds = Arrays.asList(7L, 8L, 9L);

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(persistencePort.findTechnologyIdsByCapabilityId(capabilityId)).thenReturn(Flux.fromIterable(technologyIds));

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectNextMatches(result -> result.getId().equals(100L))
            .verifyComplete();
    }
}
