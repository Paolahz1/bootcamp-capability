package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.exception.CapabilityInUseException;
import com.bootcamp.capabilityservice.domain.exception.CapabilityNotFoundException;
import com.bootcamp.capabilityservice.domain.exception.ExceptionResponse;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para DeleteCapabilityUseCase.
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
@ExtendWith(MockitoExtension.class)
class DeleteCapabilityUseCaseTest {

    @Mock
    private ICapabilityPersistencePort persistencePort;

    @Mock
    private IBootcampClientPort bootcampClientPort;

    private DeleteCapabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteCapabilityUseCase(persistencePort, bootcampClientPort);
    }

    private Capability createCapability(Long id, String name) {
        return new Capability(id, name, "Description for " + name, 
            Arrays.asList(1L, 2L, 3L), LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * Helper method to set up default mocks for all external calls.
     * The use case builds the reactive chain eagerly, so all methods are invoked.
     */
    private void setupDefaultMocks(Long capabilityId) {
        lenient().when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(0L));
        lenient().when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should delete capability when it exists and is not in use")
    void shouldDeleteCapabilityWhenExistsAndNotInUse() {
        // Given
        Long capabilityId = 1L;
        Capability capability = createCapability(capabilityId, "Backend Development");

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(0L));
        when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .verifyComplete();

        verify(persistencePort).deleteById(capabilityId);
    }

    @Test
    @DisplayName("Should throw CapabilityNotFoundException when capability does not exist")
    void shouldThrowNotFoundExceptionWhenCapabilityNotExists() {
        // Given
        Long nonExistentId = 999L;

        when(persistencePort.findById(nonExistentId)).thenReturn(Mono.empty());
        setupDefaultMocks(nonExistentId);

        // When & Then
        StepVerifier.create(useCase.execute(nonExistentId))
            .expectErrorMatches(error -> error instanceof CapabilityNotFoundException)
            .verify();
    }

    @Test
    @DisplayName("Should throw CapabilityInUseException when capability is used by one bootcamp")
    void shouldThrowInUseExceptionWhenUsedByOneBootcamp() {
        // Given
        Long capabilityId = 2L;
        Capability capability = createCapability(capabilityId, "Frontend Development");

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(1L));
        lenient().when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectErrorMatches(error -> 
                error instanceof CapabilityInUseException &&
                ((CapabilityInUseException) error).getExceptionResponse() == ExceptionResponse.DELETE_FAILED)
            .verify();
    }

    @Test
    @DisplayName("Should throw CapabilityInUseException when capability is used by multiple bootcamps")
    void shouldThrowInUseExceptionWhenUsedByMultipleBootcamps() {
        // Given
        Long capabilityId = 3L;
        Capability capability = createCapability(capabilityId, "Full Stack");

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(5L));
        lenient().when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectErrorMatches(error -> 
                error instanceof CapabilityInUseException &&
                ((CapabilityInUseException) error).getExceptionResponse() == ExceptionResponse.DELETE_FAILED)
            .verify();
    }

    @Test
    @DisplayName("Should validate capability exists before checking bootcamp usage")
    void shouldValidateExistenceBeforeCheckingUsage() {
        // Given
        Long nonExistentId = 100L;

        when(persistencePort.findById(nonExistentId)).thenReturn(Mono.empty());
        setupDefaultMocks(nonExistentId);

        // When & Then
        StepVerifier.create(useCase.execute(nonExistentId))
            .expectError(CapabilityNotFoundException.class)
            .verify();
    }

    @Test
    @DisplayName("Should successfully delete capability with zero bootcamp usage")
    void shouldSuccessfullyDeleteCapabilityWithZeroUsage() {
        // Given
        Long capabilityId = 4L;
        Capability capability = createCapability(capabilityId, "DevOps");

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(0L));
        when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .verifyComplete();

        verify(bootcampClientPort).countBootcampsByCapabilityId(capabilityId);
        verify(persistencePort).deleteById(capabilityId);
    }

    @Test
    @DisplayName("Should handle different capability IDs correctly")
    void shouldHandleDifferentCapabilityIds() {
        // Given
        Long capabilityId = 500L;
        Capability capability = createCapability(capabilityId, "Mobile Development");

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(0L));
        when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw CapabilityInUseException with correct response when capability is in use")
    void shouldThrowCorrectExceptionWhenCapabilityInUse() {
        // Given
        Long capabilityId = 6L;
        Capability capability = createCapability(capabilityId, "Data Science");

        when(persistencePort.findById(capabilityId)).thenReturn(Mono.just(capability));
        when(bootcampClientPort.countBootcampsByCapabilityId(capabilityId)).thenReturn(Mono.just(10L));
        lenient().when(persistencePort.deleteById(capabilityId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(capabilityId))
            .expectErrorMatches(error -> {
                if (error instanceof CapabilityInUseException) {
                    CapabilityInUseException ex = (CapabilityInUseException) error;
                    return ex.getExceptionResponse() == ExceptionResponse.DELETE_FAILED;
                }
                return false;
            })
            .verify();
    }
}
