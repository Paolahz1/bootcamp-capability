package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.IPersonClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para DeleteBootcampSagaUseCase.
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8
 */
@ExtendWith(MockitoExtension.class)
class DeleteBootcampSagaUseCaseTest {

    @Mock
    private IPersonClientPort personClientPort;

    @Mock
    private IBootcampClientPort bootcampClientPort;

    @Mock
    private ICapabilityPersistencePort capabilityPersistencePort;

    private DeleteBootcampSagaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBootcampSagaUseCase(personClientPort, bootcampClientPort, capabilityPersistencePort);
    }

    /**
     * Tests for successful Saga completion (28.3)
     */
    @Nested
    @DisplayName("Successful Saga Tests")
    class SuccessfulSagaTests {

        @Test
        @DisplayName("Should complete Saga successfully with orphan capability cleanup")
        void shouldCompleteSagaSuccessfullyWithOrphanCleanup() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Arrays.asList(10L, 20L, 30L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            
            // Capability 10 is orphan (count = 0), others are still in use
            when(bootcampClientPort.countBootcampsByCapabilityId(10L)).thenReturn(Mono.just(0L));
            when(bootcampClientPort.countBootcampsByCapabilityId(20L)).thenReturn(Mono.just(2L));
            when(bootcampClientPort.countBootcampsByCapabilityId(30L)).thenReturn(Mono.just(1L));
            
            when(capabilityPersistencePort.deleteById(10L)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            // Verify orphan capability was deleted
            verify(capabilityPersistencePort).deleteById(10L);
            // Verify non-orphan capabilities were not deleted
            verify(capabilityPersistencePort, never()).deleteById(20L);
            verify(capabilityPersistencePort, never()).deleteById(30L);
        }

        @Test
        @DisplayName("Should complete Saga successfully without orphan capabilities")
        void shouldCompleteSagaSuccessfullyWithoutOrphanCapabilities() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Arrays.asList(10L, 20L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            
            // All capabilities are still in use
            when(bootcampClientPort.countBootcampsByCapabilityId(10L)).thenReturn(Mono.just(1L));
            when(bootcampClientPort.countBootcampsByCapabilityId(20L)).thenReturn(Mono.just(3L));

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            // Verify no capabilities were deleted
            verify(capabilityPersistencePort, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should complete Saga successfully with no capabilities")
        void shouldCompleteSagaSuccessfullyWithNoCapabilities() {
            // Given
            Long bootcampId = 1L;

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.empty());
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            verify(capabilityPersistencePort, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should execute Saga steps in correct order")
        void shouldExecuteSagaStepsInCorrectOrder() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Collections.singletonList(10L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.countBootcampsByCapabilityId(10L))
                .thenReturn(Mono.just(0L));
            when(capabilityPersistencePort.deleteById(10L))
                .thenReturn(Mono.empty());

            // When
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            // Then - verify order
            InOrder inOrder = inOrder(personClientPort, bootcampClientPort, capabilityPersistencePort);
            inOrder.verify(personClientPort).deleteEnrollmentsByBootcampId(bootcampId);
            inOrder.verify(bootcampClientPort).getCapabilityIdsByBootcampId(bootcampId);
            inOrder.verify(bootcampClientPort).deleteBootcamp(bootcampId);
            inOrder.verify(bootcampClientPort).countBootcampsByCapabilityId(10L);
            inOrder.verify(capabilityPersistencePort).deleteById(10L);
        }

        @Test
        @DisplayName("Should delete all orphan capabilities")
        void shouldDeleteAllOrphanCapabilities() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Arrays.asList(10L, 20L, 30L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            
            // All capabilities are orphans
            when(bootcampClientPort.countBootcampsByCapabilityId(10L)).thenReturn(Mono.just(0L));
            when(bootcampClientPort.countBootcampsByCapabilityId(20L)).thenReturn(Mono.just(0L));
            when(bootcampClientPort.countBootcampsByCapabilityId(30L)).thenReturn(Mono.just(0L));
            
            when(capabilityPersistencePort.deleteById(10L)).thenReturn(Mono.empty());
            when(capabilityPersistencePort.deleteById(20L)).thenReturn(Mono.empty());
            when(capabilityPersistencePort.deleteById(30L)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            // Verify all orphan capabilities were deleted
            verify(capabilityPersistencePort).deleteById(10L);
            verify(capabilityPersistencePort).deleteById(20L);
            verify(capabilityPersistencePort).deleteById(30L);
        }
    }

    /**
     * Tests for Saga failure and compensation (28.4)
     */
    @Nested
    @DisplayName("Saga Failure and Compensation Tests")
    class SagaFailureTests {

        @Test
        @DisplayName("Should fail Saga when enrollment deletion fails")
        void shouldFailSagaWhenEnrollmentDeletionFails() {
            // Given
            Long bootcampId = 1L;
            RuntimeException enrollmentError = new RuntimeException("Person Service unavailable");

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.error(enrollmentError));
            // Need to mock these because the chain is built eagerly (but they won't be executed)
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.empty());
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Person Service unavailable"))
                .verify();

            // Verify capability cleanup was not executed
            verify(capabilityPersistencePort, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should fail Saga when bootcamp deletion fails")
        void shouldFailSagaWhenBootcampDeletionFails() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Arrays.asList(10L, 20L);
            RuntimeException bootcampError = new RuntimeException("Bootcamp Service unavailable");

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.error(bootcampError));

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Bootcamp Service unavailable"))
                .verify();

            // Verify capability cleanup was not executed
            verify(bootcampClientPort, never()).countBootcampsByCapabilityId(anyLong());
            verify(capabilityPersistencePort, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should fail Saga when getting capability IDs fails")
        void shouldFailSagaWhenGettingCapabilityIdsFails() {
            // Given
            Long bootcampId = 1L;
            RuntimeException capabilityError = new RuntimeException("Failed to get capabilities");

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.error(capabilityError));
            // Need to mock this because the chain is built eagerly
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Failed to get capabilities"))
                .verify();

            // Verify capability cleanup was not executed
            verify(capabilityPersistencePort, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should fail Saga when capability count check fails")
        void shouldFailSagaWhenCapabilityCountCheckFails() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Collections.singletonList(10L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.countBootcampsByCapabilityId(10L))
                .thenReturn(Mono.error(new RuntimeException("Count failed")));

            // When & Then - The saga should fail due to the error in capability count
            StepVerifier.create(useCase.execute(bootcampId))
                .expectError(RuntimeException.class)
                .verify();
        }

        @Test
        @DisplayName("Should fail Saga when capability deletion fails")
        void shouldFailSagaWhenCapabilityDeletionFails() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Collections.singletonList(10L);
            RuntimeException deleteError = new RuntimeException("Failed to delete capability");

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.countBootcampsByCapabilityId(10L))
                .thenReturn(Mono.just(0L));
            when(capabilityPersistencePort.deleteById(10L))
                .thenReturn(Mono.error(deleteError));

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Failed to delete capability"))
                .verify();
        }
    }

    /**
     * Additional edge case tests
     */
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle bootcamp with single capability")
        void shouldHandleBootcampWithSingleCapability() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Collections.singletonList(10L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.countBootcampsByCapabilityId(10L))
                .thenReturn(Mono.just(0L));
            when(capabilityPersistencePort.deleteById(10L))
                .thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            verify(capabilityPersistencePort).deleteById(10L);
        }

        @Test
        @DisplayName("Should handle bootcamp with many capabilities")
        void shouldHandleBootcampWithManyCapabilities() {
            // Given
            Long bootcampId = 1L;
            List<Long> capabilityIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

            when(personClientPort.deleteEnrollmentsByBootcampId(bootcampId))
                .thenReturn(Mono.empty());
            when(bootcampClientPort.getCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.fromIterable(capabilityIds));
            when(bootcampClientPort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.empty());
            
            // All capabilities are still in use
            for (Long capId : capabilityIds) {
                when(bootcampClientPort.countBootcampsByCapabilityId(capId))
                    .thenReturn(Mono.just(1L));
            }

            // When & Then
            StepVerifier.create(useCase.execute(bootcampId))
                .verifyComplete();

            // Verify no capabilities were deleted
            verify(capabilityPersistencePort, never()).deleteById(anyLong());
        }
    }
}
