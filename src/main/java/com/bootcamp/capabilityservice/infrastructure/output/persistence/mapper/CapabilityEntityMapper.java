package com.bootcamp.capabilityservice.infrastructure.output.persistence.mapper;

import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.infrastructure.output.persistence.entity.CapabilityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper para conversión entre CapabilityEntity y Capability domain.
 */
@Mapper(componentModel = "spring")
public interface CapabilityEntityMapper {

    /**
     * Convierte entidad a modelo de dominio.
     */
    @Mapping(target = "technologyIds", ignore = true)
    Capability toDomain(CapabilityEntity entity);

    /**
     * Convierte lista de entidades a lista de modelos de dominio.
     */
    List<Capability> toDomainList(List<CapabilityEntity> entities);

    /**
     * Convierte modelo de dominio a entidad.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CapabilityEntity toEntity(Capability capability);

    /**
     * Convierte modelo de dominio a entidad para actualización (preserva ID).
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CapabilityEntity toEntityForUpdate(Capability capability);
}
