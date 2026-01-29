package com.bootcamp.capabilityservice.application.mapper;

import com.bootcamp.capabilityservice.application.dto.response.TechnologyResponse;
import com.bootcamp.capabilityservice.domain.model.Technology;
import com.bootcamp.capabilityservice.infrastructure.output.client.dto.TechnologyDto;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para conversión entre Technology domain, DTOs externos y responses.
 */
@Mapper(componentModel = "spring")
public interface ITechnologyMapper {

    /**
     * Convierte DTO externo a modelo de dominio.
     */
    Technology toDomain(TechnologyDto dto);

    /**
     * Convierte lista de DTOs externos a lista de modelos de dominio.
     */
    List<Technology> toDomainList(List<TechnologyDto> dtos);

    /**
     * Convierte modelo de dominio a response.
     */
    TechnologyResponse toResponse(Technology technology);

    /**
     * Convierte lista de modelos de dominio a lista de responses.
     */
    List<TechnologyResponse> toResponseList(List<Technology> technologies);

    /**
     * Convierte DTO externo directamente a response.
     */
    TechnologyResponse dtoToResponse(TechnologyDto dto);

    /**
     * Convierte lista de DTOs externos directamente a lista de responses.
     */
    List<TechnologyResponse> dtoToResponseList(List<TechnologyDto> dtos);
}
