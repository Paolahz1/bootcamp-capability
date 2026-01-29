package com.bootcamp.capabilityservice.infrastructure.output.persistence.adapter;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Page;
import com.bootcamp.capabilityservice.domain.spi.ICapabilityPersistencePort;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.entity.CapabilityEntity;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.mapper.CapabilityEntityMapper;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.repository.ICapabilityRepository;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.repository.ICapabilityTechnologyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador de persistencia para capacidades.
 * Implementa el puerto de salida usando R2DBC.
 */
@Component
public class CapabilityPersistenceAdapter implements ICapabilityPersistencePort {

    private final ICapabilityRepository capabilityRepository;
    private final ICapabilityTechnologyRepository capabilityTechnologyRepository;
    private final CapabilityEntityMapper mapper;
    private final TransactionalOperator transactionalOperator;

    public CapabilityPersistenceAdapter(
            ICapabilityRepository capabilityRepository,
            ICapabilityTechnologyRepository capabilityTechnologyRepository,
            CapabilityEntityMapper mapper,
            TransactionalOperator transactionalOperator) {
        this.capabilityRepository = capabilityRepository;
        this.capabilityTechnologyRepository = capabilityTechnologyRepository;
        this.mapper = mapper;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Capability> save(Capability capability, List<Long> technologyIds) {
        return Mono.defer(() -> {
            CapabilityEntity entity = mapper.toEntity(capability);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            return capabilityRepository.save(entity)
                .flatMap(savedEntity -> saveRelations(savedEntity.getId(), technologyIds)
                    .then(Mono.just(savedEntity)))
                .map(savedEntity -> {
                    Capability saved = mapper.toDomain(savedEntity);
                    saved.setTechnologyIds(technologyIds);
                    return saved;
                });
        }).as(transactionalOperator::transactional);
    }

    private Mono<Void> saveRelations(Long capabilityId, List<Long> technologyIds) {
        return Flux.fromIterable(technologyIds)
            .flatMap(techId -> capabilityTechnologyRepository.insertRelation(capabilityId, techId))
            .then();
    }

    @Override
    public Mono<Page<Capability>> findAll(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return capabilityRepository.findAllBy(pageable)
            .map(mapper::toDomain)
            .collectList()
            .zipWith(capabilityRepository.count())
            .map(tuple -> new Page<>(
                tuple.getT1(),
                page,
                size,
                tuple.getT2()
            ));
    }

    @Override
    public Mono<Capability> findById(Long id) {
        return capabilityRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Capability> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return capabilityRepository.findByIdIn(ids)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return capabilityRepository.existsByName(name);
    }

    @Override
    public Mono<Long> countByTechnologyId(Long technologyId) {
        return capabilityTechnologyRepository.countByTechnologyId(technologyId);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return capabilityRepository.deleteById(id);
    }

    @Override
    public Flux<Long> findTechnologyIdsByCapabilityId(Long capabilityId) {
        return capabilityTechnologyRepository.findTechnologyIdsByCapabilityId(capabilityId);
    }
}
