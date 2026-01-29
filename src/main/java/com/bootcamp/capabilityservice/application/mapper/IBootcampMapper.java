package com.bootcamp.capabilityservice.application.mapper;

import com.bootcamp.capabilityservice.application.dto.request.CreateBootcampRequest;
import com.bootcamp.capabilityservice.application.dto.response.BootcampResponse;
import com.bootcamp.capabilityservice.application.dto.response.CapabilityResponse;
import com.bootcamp.capabilityservice.domain.model.Bootcamp;
import com.bootcamp.capabilityservice.infrastructure.output.client.dto.BootcampDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ICapabilityMapper.class})
public interface IBootcampMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "capabilities", ignore = true)
    Bootcamp toDomain(CreateBootcampRequest request);


    @Mapping(target = "capabilities", ignore = true)
    Bootcamp toDomain(BootcampDto dto);

  
    List<Bootcamp> toDomainList(List<BootcampDto> dtos);

    @Mapping(target = "capabilities", ignore = true)
    BootcampResponse toResponse(Bootcamp bootcamp);

    default BootcampResponse toResponseWithCapabilities(Bootcamp bootcamp, List<CapabilityResponse> capabilities) {
        BootcampResponse response = toResponse(bootcamp);
        if (capabilities != null) {
            response.setCapabilities(capabilities);
        }
        return response;
    }

    List<BootcampResponse> toResponseList(List<Bootcamp> bootcamps);

    @Mapping(target = "id", ignore = true)
    BootcampDto toDto(Bootcamp bootcamp);
}
