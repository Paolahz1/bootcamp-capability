package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.exception.CapabilityNotFoundException;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.domain.model.BootcampReport;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Technology;
import com.bootcamp.capabilityservice.domain.spi.IBootcampClientPort;
import com.bootcamp.capabilityservice.domain.spi.IBootcampReportPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.domain.spi.ITechnologyClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para CreateBootcampUseCase.
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateBootcampUseCaseTest {

    @Mock
    private ICapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private IBootcampClientPort bootcampClientPort;

    @Mock
    private IBootcampReportPersistencePort reportPersistencePort;

    @Mock
    private ITechnologyClientPort technologyClientPort;

    private CreateBootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBootcampUseCase(capabilityPersistencePort, bootcampClientPort, 
            reportPersistencePort, technologyClientPort);
    }

    private Bootcamp createTestBootcamp(String name, String description) {
        return new Bootcamp(null, name, description, 
            LocalDate.now().plusDays(30), LocalDate.now().plusDays(120), null);
    }

    private Bootcamp createSavedBootcamp(Long id, String name, String description, List<Long> capabilityIds) {
        return new Bootcamp(id, name, description, 
            LocalDate.now().plusDays(30), LocalDate.now().plusDays(120), capabilityIds);
    }

    private Capability createCapability(Long id, String name) {
        return new Capability(id, name, "Description for " + name, null, null, null);
    }

    private void setupReportMocks() {
        when(capabilityPersistencePort.findTechnologyIdsByCapabilityId(anyLong()))
            .thenReturn(Flux.just(1L, 2L));
        when(technologyClientPort.getTechnologiesByIds(anyList()))
            .thenReturn(Flux.just(
                new Technology(1L, "Java", "Java language"),
                new Technology(2L, "Spring", "Spring framework")
            ));
        when(reportPersistencePort.save(any(BootcampReport.class)))
            .thenReturn(Mono.just(new BootcampReport()));
    }

    @Test
    @DisplayName("Should create bootcamp when all capabilities exist")
    void shouldCreateBootcampWhenAllCapabilitiesExist() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("Full Stack Bootcamp", "Complete development bootcamp");
        List<Long> capabilityIds = Arrays.asList(1L, 2L, 3L);
        
        List<Capability> foundCapabilities = Arrays.asList(
            createCapability(1L, "Backend"),
            createCapability(2L, "Frontend"),
            createCapability(3L, "DevOps")
        );
        
        Bootcamp savedBootcamp = createSavedBootcamp(1L, "Full Stack Bootcamp", 
            "Complete development bootcamp", capabilityIds);

        when(capabilityPersistencePort.findByIds(capabilityIds))
            .thenReturn(Flux.fromIterable(foundCapabilities));
        when(bootcampClientPort.createBootcamp(any(Bootcamp.class)))
            .thenReturn(Mono.just(savedBootcamp));
        setupReportMocks();

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, capabilityIds))
            .expectNextMatches(result -> 
                result.getId().equals(1L) && 
                result.getName().equals("Full Stack Bootcamp") &&
                result.getCapabilityIds().equals(capabilityIds))
            .verifyComplete();

        verify(bootcampClientPort).createBootcamp(any(Bootcamp.class));
    }

    @Test
    @DisplayName("Should reject bootcamp when some capabilities do not exist")
    void shouldRejectBootcampWhenSomeCapabilitiesNotExist() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("Invalid Bootcamp", "Missing capabilities");
        List<Long> capabilityIds = Arrays.asList(1L, 2L, 999L); // 999L doesn't exist
        
        List<Capability> foundCapabilities = Arrays.asList(
            createCapability(1L, "Backend"),
            createCapability(2L, "Frontend")
        ); // Only 2 found, but 3 requested

        when(capabilityPersistencePort.findByIds(capabilityIds))
            .thenReturn(Flux.fromIterable(foundCapabilities));

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, capabilityIds))
            .expectError(CapabilityNotFoundException.class)
            .verify();

        verify(bootcampClientPort, never()).createBootcamp(any());
    }

    @Test
    @DisplayName("Should reject bootcamp when all capabilities do not exist")
    void shouldRejectBootcampWhenAllCapabilitiesNotExist() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("No Capabilities", "All missing");
        List<Long> capabilityIds = Arrays.asList(100L, 200L, 300L);

        when(capabilityPersistencePort.findByIds(capabilityIds))
            .thenReturn(Flux.empty()); // None found

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, capabilityIds))
            .expectError(CapabilityNotFoundException.class)
            .verify();

        verify(bootcampClientPort, never()).createBootcamp(any());
    }

    @Test
    @DisplayName("Should create bootcamp with empty capability list")
    void shouldCreateBootcampWithEmptyCapabilityList() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("Basic Bootcamp", "No capabilities");
        List<Long> capabilityIds = Collections.emptyList();
        
        Bootcamp savedBootcamp = createSavedBootcamp(1L, "Basic Bootcamp", 
            "No capabilities", capabilityIds);

        when(bootcampClientPort.createBootcamp(any(Bootcamp.class)))
            .thenReturn(Mono.just(savedBootcamp));
        when(reportPersistencePort.save(any(BootcampReport.class)))
            .thenReturn(Mono.just(new BootcampReport()));

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, capabilityIds))
            .expectNextMatches(result -> result.getId().equals(1L))
            .verifyComplete();

        verify(bootcampClientPort).createBootcamp(any(Bootcamp.class));
    }

    @Test
    @DisplayName("Should create bootcamp with null capability list")
    void shouldCreateBootcampWithNullCapabilityList() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("Null Capabilities", "Null list");
        
        Bootcamp savedBootcamp = createSavedBootcamp(1L, "Null Capabilities", 
            "Null list", null);

        when(bootcampClientPort.createBootcamp(any(Bootcamp.class)))
            .thenReturn(Mono.just(savedBootcamp));
        when(reportPersistencePort.save(any(BootcampReport.class)))
            .thenReturn(Mono.just(new BootcampReport()));

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, null))
            .expectNextMatches(result -> result.getId().equals(1L))
            .verifyComplete();

        verify(bootcampClientPort).createBootcamp(any(Bootcamp.class));
    }

    @Test
    @DisplayName("Should propagate error from Bootcamp Service")
    void shouldPropagateErrorFromBootcampService() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("Error Bootcamp", "Will fail");
        List<Long> capabilityIds = Arrays.asList(1L, 2L, 3L);
        
        List<Capability> foundCapabilities = Arrays.asList(
            createCapability(1L, "Backend"),
            createCapability(2L, "Frontend"),
            createCapability(3L, "DevOps")
        );

        when(capabilityPersistencePort.findByIds(capabilityIds))
            .thenReturn(Flux.fromIterable(foundCapabilities));
        when(bootcampClientPort.createBootcamp(any(Bootcamp.class)))
            .thenReturn(Mono.error(new RuntimeException("Bootcamp Service unavailable")));

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, capabilityIds))
            .expectErrorMatches(error -> 
                error instanceof RuntimeException &&
                error.getMessage().equals("Bootcamp Service unavailable"))
            .verify();
    }

    @Test
    @DisplayName("Should set capability IDs on bootcamp before creating")
    void shouldSetCapabilityIdsOnBootcampBeforeCreating() {
        // Given
        Bootcamp bootcamp = createTestBootcamp("Test Bootcamp", "Test description");
        List<Long> capabilityIds = Arrays.asList(1L, 2L);
        
        List<Capability> foundCapabilities = Arrays.asList(
            createCapability(1L, "Backend"),
            createCapability(2L, "Frontend")
        );
        
        Bootcamp savedBootcamp = createSavedBootcamp(1L, "Test Bootcamp", 
            "Test description", capabilityIds);

        when(capabilityPersistencePort.findByIds(capabilityIds))
            .thenReturn(Flux.fromIterable(foundCapabilities));
        when(bootcampClientPort.createBootcamp(any(Bootcamp.class)))
            .thenAnswer(invocation -> {
                Bootcamp arg = invocation.getArgument(0);
                // Verify capability IDs were set
                if (arg.getCapabilityIds() != null && arg.getCapabilityIds().equals(capabilityIds)) {
                    return Mono.just(savedBootcamp);
                }
                return Mono.error(new AssertionError("Capability IDs not set correctly"));
            });
        
        // Setup report mocks for fire-and-forget
        when(capabilityPersistencePort.findTechnologyIdsByCapabilityId(anyLong()))
            .thenReturn(Flux.just(1L, 2L));
        when(technologyClientPort.getTechnologiesByIds(anyList()))
            .thenReturn(Flux.just(
                new Technology(1L, "Java", "Java language"),
                new Technology(2L, "Spring", "Spring framework")
            ));
        when(reportPersistencePort.save(any(BootcampReport.class)))
            .thenReturn(Mono.just(new BootcampReport()));

        // When & Then
        StepVerifier.create(useCase.execute(bootcamp, capabilityIds))
            .expectNextMatches(result -> result.getCapabilityIds().equals(capabilityIds))
            .verifyComplete();
    }
}
