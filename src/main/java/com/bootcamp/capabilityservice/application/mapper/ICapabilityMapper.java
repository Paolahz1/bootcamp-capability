package com.bootcamp.capabilityservice.application.mapper;

import com.bootcamp.capabilityservice.application.dto.request.CreateCapabilityRequest;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.application.dto.response.TechnologyResponse;
import com.bootcamp.capabilityservice.domain.model.Capability;
import com.bootcamp.capabilityservice.domain.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ICapabilityMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "technologyIds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Capability toDomain(CreateCapabilityRequest request);

   /**
     * Convierte modelo de dominio a response.
     * Las tecnologías deben ser mapeadas por separado.
     */ 
    @Mapping(target = "technologies", ignore = true)
    CapabilityResponse toResponse(Capability capability);

    default CapabilityResponse toResponseWithTechnologies(Capability capability, List<Technology> technologies) {
        CapabilityResponse response = toResponse(capability);
        if (technologies != null) {
            response.setTechnologies(toTechnologyResponseList(technologies));
        }
        return response;
    }

 
    List<CapabilityResponse> toResponseList(List<Capability> capabilities);


    TechnologyResponse toTechnologyResponse(Technology technology);

    
    List<TechnologyResponse> toTechnologyResponseList(List<Technology> technologies);
}
