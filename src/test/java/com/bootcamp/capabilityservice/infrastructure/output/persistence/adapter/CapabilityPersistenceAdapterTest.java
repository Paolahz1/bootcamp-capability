package com.bootcamp.capabilityservice.infrastructure.output.persistence.adapter;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.entity.CapabilityEntity;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.mapper.CapabilityEntityMapper;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.repository.ICapabilityRepository;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.repository.ICapabilityTechnologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para CapabilityPersistenceAdapter.
 * Validates: Requirements 1.6, 1.7, 2.1, 2.4, 3.1, 3.3, 5.1, 5.2, 6.5
 */
@ExtendWith(MockitoExtension.class)
class CapabilityPersistenceAdapterTest {

    @Mock
    private ICapabilityRepository capabilityRepository;

    @Mock
    private ICapabilityTechnologyRepository capabilityTechnologyRepository;

    @Mock
    private CapabilityEntityMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    private CapabilityPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CapabilityPersistenceAdapter(
            capabilityRepository,
            capabilityTechnologyRepository,
            mapper,
            transactionalOperator
        );
    }

    private CapabilityEntity createEntity(Long id, String name, String description) {
        return new CapabilityEntity(id, name, description, LocalDateTime.now(), LocalDateTime.now());
    }

    private Capability createCapability(Long id, String name, String description, List<Long> techIds) {
        return new Capability(id, name, description, techIds, LocalDateTime.now(), LocalDateTime.now());
    }

    // ========== Tests for save() ==========

    @Test
    @DisplayName("Should save capability with technology relations")
    void shouldSaveCapabilityWithTechnologyRelations() {
        // Given
        Capability capability = createCapability(null, "Backend", "Backend technologies", null);
        List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);
        CapabilityEntity entity = createEntity(null, "Backend", "Backend technologies");
        CapabilityEntity savedEntity = createEntity(1L, "Backend", "Backend technologies");
        Capability savedCapability = createCapability(1L, "Backend", "Backend technologies", technologyIds);

        when(mapper.toEntity(capability)).thenReturn(entity);
        when(capabilityRepository.save(any(CapabilityEntity.class))).thenReturn(Mono.just(savedEntity));
        when(capabilityTechnologyRepository.insertRelation(anyLong(), anyLong())).thenReturn(Mono.empty());
        when(mapper.toDomain(savedEntity)).thenReturn(savedCapability);
        when(transactionalOperator.transactional(any(Mono.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        StepVerifier.create(adapter.save(capability, technologyIds))
            .expectNextMatches(result -> 
                result.getId().equals(1L) && 
                result.getName().equals("Backend") &&
                result.getTechnologyIds().size() == 3)
            .verifyComplete();

        verify(capabilityRepository).save(any(CapabilityEntity.class));
    }

    // ========== Tests for findAll() ==========

    @Test
    @DisplayName("Should find all capabilities with pagination")
    void shouldFindAllCapabilitiesWithPagination() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";
        
        CapabilityEntity entity1 = createEntity(1L, "Backend", "Backend desc");
        CapabilityEntity entity2 = createEntity(2L, "Frontend", "Frontend desc");
        Capability capability1 = createCapability(1L, "Backend", "Backend desc", null);
        Capability capability2 = createCapability(2L, "Frontend", "Frontend desc", null);

        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        when(capabilityRepository.findAllBy(any(Pageable.class)))
            .thenReturn(Flux.just(entity1, entity2));
        when(capabilityRepository.count()).thenReturn(Mono.just(2L));
        when(mapper.toDomain(entity1)).thenReturn(capability1);
        when(mapper.toDomain(entity2)).thenReturn(capability2);

        // When & Then
        StepVerifier.create(adapter.findAll(page, size, sortBy, direction))
            .expectNextMatches(result -> 
                result.getContent().size() == 2 &&
                result.getPageNumber() == 0 &&
                result.getPageSize() == 10 &&
                result.getTotalElements() == 2)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty page when no capabilities exist")
    void shouldReturnEmptyPageWhenNoCapabilities() {
        // Given
        when(capabilityRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(capabilityRepository.count()).thenReturn(Mono.just(0L));

        // When & Then
        StepVerifier.create(adapter.findAll(0, 10, "name", "ASC"))
            .expectNextMatches(result -> 
                result.getContent().isEmpty() &&
                result.getTotalElements() == 0)
            .verifyComplete();
    }

    // ========== Tests for findById() ==========

    @Test
    @DisplayName("Should find capability by ID")
    void shouldFindCapabilityById() {
        // Given
        Long id = 1L;
        CapabilityEntity entity = createEntity(id, "Backend", "Backend desc");
        Capability capability = createCapability(id, "Backend", "Backend desc", null);

        when(capabilityRepository.findById(id)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(capability);

        // When & Then
        StepVerifier.create(adapter.findById(id))
            .expectNextMatches(result -> 
                result.getId().equals(1L) && 
                result.getName().equals("Backend"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when capability not found by ID")
    void shouldReturnEmptyWhenCapabilityNotFoundById() {
        // Given
        Long id = 999L;
        when(capabilityRepository.findById(id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.findById(id))
            .verifyComplete();
    }

    // ========== Tests for findByIds() ==========

    @Test
    @DisplayName("Should find capabilities by multiple IDs")
    void shouldFindCapabilitiesByIds() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        CapabilityEntity entity1 = createEntity(1L, "Backend", "Backend desc");
        CapabilityEntity entity2 = createEntity(2L, "Frontend", "Frontend desc");
        Capability capability1 = createCapability(1L, "Backend", "Backend desc", null);
        Capability capability2 = createCapability(2L, "Frontend", "Frontend desc", null);

        when(capabilityRepository.findByIdIn(ids)).thenReturn(Flux.just(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(capability1);
        when(mapper.toDomain(entity2)).thenReturn(capability2);

        // When & Then
        StepVerifier.create(adapter.findByIds(ids))
            .expectNextMatches(c -> c.getId().equals(1L))
            .expectNextMatches(c -> c.getId().equals(2L))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when IDs list is empty")
    void shouldReturnEmptyFluxWhenIdsListEmpty() {
        // When & Then
        StepVerifier.create(adapter.findByIds(List.of()))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when IDs list is null")
    void shouldReturnEmptyFluxWhenIdsListNull() {
        // When & Then
        StepVerifier.create(adapter.findByIds(null))
            .verifyComplete();
    }

    // ========== Tests for existsByName() ==========

    @Test
    @DisplayName("Should return true when capability name exists")
    void shouldReturnTrueWhenNameExists() {
        // Given
        String name = "Backend";
        when(capabilityRepository.existsByName(name)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(adapter.existsByName(name))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when capability name does not exist")
    void shouldReturnFalseWhenNameNotExists() {
        // Given
        String name = "NonExistent";
        when(capabilityRepository.existsByName(name)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(adapter.existsByName(name))
            .expectNext(false)
            .verifyComplete();
    }

    // ========== Tests for countByTechnologyId() ==========

    @Test
    @DisplayName("Should count capabilities by technology ID")
    void shouldCountCapabilitiesByTechnologyId() {
        // Given
        Long technologyId = 1L;
        when(capabilityTechnologyRepository.countByTechnologyId(technologyId)).thenReturn(Mono.just(5L));

        // When & Then
        StepVerifier.create(adapter.countByTechnologyId(technologyId))
            .expectNext(5L)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return zero when no capabilities use technology")
    void shouldReturnZeroWhenNoCapabilitiesUseTechnology() {
        // Given
        Long technologyId = 999L;
        when(capabilityTechnologyRepository.countByTechnologyId(technologyId)).thenReturn(Mono.just(0L));

        // When & Then
        StepVerifier.create(adapter.countByTechnologyId(technologyId))
            .expectNext(0L)
            .verifyComplete();
    }

    // ========== Tests for deleteById() ==========

    @Test
    @DisplayName("Should delete capability by ID")
    void shouldDeleteCapabilityById() {
        // Given
        Long id = 1L;
        when(capabilityRepository.deleteById(id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.deleteById(id))
            .verifyComplete();

        verify(capabilityRepository).deleteById(id);
    }

    // ========== Tests for findTechnologyIdsByCapabilityId() ==========

    @Test
    @DisplayName("Should find technology IDs by capability ID")
    void shouldFindTechnologyIdsByCapabilityId() {
        // Given
        Long capabilityId = 1L;
        when(capabilityTechnologyRepository.findTechnologyIdsByCapabilityId(capabilityId))
            .thenReturn(Flux.just(1L, 2L, 3L));

        // When & Then
        StepVerifier.create(adapter.findTechnologyIdsByCapabilityId(capabilityId))
            .expectNext(1L)
            .expectNext(2L)
            .expectNext(3L)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when capability has no technologies")
    void shouldReturnEmptyFluxWhenCapabilityHasNoTechnologies() {
        // Given
        Long capabilityId = 1L;
        when(capabilityTechnologyRepository.findTechnologyIdsByCapabilityId(capabilityId))
            .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(adapter.findTechnologyIdsByCapabilityId(capabilityId))
            .verifyComplete();
    }
}
