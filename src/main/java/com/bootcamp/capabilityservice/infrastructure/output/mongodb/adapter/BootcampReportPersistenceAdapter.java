package com.bootcamp.capabilityservice.infrastructure.output.mongodb.adapter;

import com.bootcamp.capabilityservice.domain.model.BootcampReport;
import com.bootcamp.capabilityservice.domain.spi.IBootcampReportPersistencePort;
import com.bootcamp.capabilityservice.infrastructure.output.mongodb.document.BootcampReportDocument;
import com.bootcamp.capabilityservice.infrastructure.output.mongodb.mapper.BootcampReportDocumentMapper;
import com.bootcamp.capabilityservice.infrastructure.output.mongodb.repository.IBootcampReportRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Adaptador de persistencia para reportes de bootcamps en MongoDB.
 * Implementa el puerto de salida IBootcampReportPersistencePort.
 */
@Component
public class BootcampReportPersistenceAdapter implements IBootcampReportPersistencePort {

    private final IBootcampReportRepository repository;
    private final BootcampReportDocumentMapper mapper;

    public BootcampReportPersistenceAdapter(IBootcampReportRepository repository,
                                             BootcampReportDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<BootcampReport> save(BootcampReport report) {
        BootcampReportDocument document = mapper.toDocument(report);
        LocalDateTime now = LocalDateTime.now();
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        return repository.save(document)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<BootcampReport> findByBootcampId(Long bootcampId) {
        return repository.findByBootcampId(bootcampId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<BootcampReport> findTopByEnrollmentCount() {
        return repository.findFirstByOrderByEnrollmentCountDesc()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> incrementEnrollmentCount(Long bootcampId) {
        return repository.findByBootcampId(bootcampId)
                .flatMap(document -> {
                    document.setEnrollmentCount(document.getEnrollmentCount() + 1);
                    document.setUpdatedAt(LocalDateTime.now());
                    return repository.save(document);
                })
                .then();
    }

    @Override
    public Mono<Void> deleteByBootcampId(Long bootcampId) {
        return repository.deleteByBootcampId(bootcampId);
    }
}
