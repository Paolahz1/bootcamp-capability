package com.bootcamp.capabilityservice.domain.usecase;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.domain.model.Technology;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para ListCapabilitiesUseCase.
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5
 */
@ExtendWith(MockitoExtension.class)
class ListCapabilitiesUseCaseTest {

    @Mock
    private ICapabilityPersistencePort persistencePort;

    @Mock
    private ITechnologyClientPort technologyClientPort;

    private ListCapabilitiesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListCapabilitiesUseCase(persistencePort, technologyClientPort);
    }

    private Capability createCapability(Long id, String name, List<Long> techIds) {
        return new Capability(id, name, "Description for " + name, techIds, 
            LocalDateTime.now(), LocalDateTime.now());
    }

    private Technology createTechnology(Long id, String name) {
        return new Technology(id, name, "Description for " + name);
    }

    @Test
    @DisplayName("Should return paginated capabilities with correct metadata")
    void shouldReturnPaginatedCapabilities() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";

        List<Capability> capabilities = Arrays.asList(
            createCapability(1L, "Backend", Arrays.asList(1L, 2L, 3L)),
            createCapability(2L, "Frontend", Arrays.asList(4L, 5L, 6L))
        );
        Page<Capability> capabilityPage = new Page<>(capabilities, page, size, 2L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));
        when(technologyClientPort.getTechnologiesByIds(anyList())).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getPageNumber() == 0 &&
                result.getPageSize() == 10 &&
                result.getTotalElements() == 2 &&
                result.getContent().size() == 2)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty page when no capabilities exist")
    void shouldReturnEmptyPageWhenNoCapabilities() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";

        Page<Capability> emptyPage = new Page<>(Collections.emptyList(), page, size, 0L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(emptyPage));

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getContent().isEmpty() &&
                result.getTotalElements() == 0 &&
                result.isFirst() &&
                result.isLast())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle pagination with multiple pages")
    void shouldHandlePaginationWithMultiplePages() {
        // Given
        int page = 1;
        int size = 2;
        String sortBy = "name";
        String direction = "ASC";

        List<Capability> capabilities = Arrays.asList(
            createCapability(3L, "DevOps", Arrays.asList(7L, 8L, 9L)),
            createCapability(4L, "Mobile", Arrays.asList(10L, 11L, 12L))
        );
        Page<Capability> capabilityPage = new Page<>(capabilities, page, size, 6L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));
        when(technologyClientPort.getTechnologiesByIds(anyList())).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getPageNumber() == 1 &&
                result.getTotalElements() == 6 &&
                result.getTotalPages() == 3 &&
                !result.isFirst() &&
                !result.isLast())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should enrich capabilities with technology data")
    void shouldEnrichCapabilitiesWithTechnologyData() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";

        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capability capability = createCapability(1L, "Backend", techIds);
        Page<Capability> capabilityPage = new Page<>(List.of(capability), page, size, 1L);

        List<Technology> technologies = Arrays.asList(
            createTechnology(1L, "Java"),
            createTechnology(2L, "Spring"),
            createTechnology(3L, "MySQL")
        );

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));
        when(technologyClientPort.getTechnologiesByIds(techIds)).thenReturn(Flux.fromIterable(technologies));

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getContent().size() == 1 &&
                result.getContent().get(0).getName().equals("Backend"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle capabilities with empty technology list")
    void shouldHandleCapabilitiesWithEmptyTechnologyList() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";

        Capability capability = createCapability(1L, "Empty Tech", Collections.emptyList());
        Page<Capability> capabilityPage = new Page<>(List.of(capability), page, size, 1L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getContent().size() == 1 &&
                result.getContent().get(0).getTechnologyIds().isEmpty())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle capabilities with null technology list")
    void shouldHandleCapabilitiesWithNullTechnologyList() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";

        Capability capability = createCapability(1L, "Null Tech", null);
        Page<Capability> capabilityPage = new Page<>(List.of(capability), page, size, 1L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> result.getContent().size() == 1)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should respect sort direction DESC")
    void shouldRespectSortDirectionDesc() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "DESC";

        List<Capability> capabilities = Arrays.asList(
            createCapability(2L, "Zebra", Arrays.asList(1L, 2L, 3L)),
            createCapability(1L, "Alpha", Arrays.asList(4L, 5L, 6L))
        );
        Page<Capability> capabilityPage = new Page<>(capabilities, page, size, 2L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));
        when(technologyClientPort.getTechnologiesByIds(anyList())).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getContent().get(0).getName().equals("Zebra") &&
                result.getContent().get(1).getName().equals("Alpha"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle last page correctly")
    void shouldHandleLastPageCorrectly() {
        // Given
        int page = 2;
        int size = 2;
        String sortBy = "name";
        String direction = "ASC";

        List<Capability> capabilities = List.of(
            createCapability(5L, "Last", Arrays.asList(1L, 2L, 3L))
        );
        Page<Capability> capabilityPage = new Page<>(capabilities, page, size, 5L);

        when(persistencePort.findAll(page, size, sortBy, direction)).thenReturn(Mono.just(capabilityPage));
        when(technologyClientPort.getTechnologiesByIds(anyList())).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.execute(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.isLast() &&
                !result.isFirst() &&
                result.getContent().size() == 1)
            .verifyComplete();
    }
}
