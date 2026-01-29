package com.bootcamp.capabilityservice.application.mapper;

import com.bootcamp.capabilityservice.application.dto.response.PageResponse;
import com.bootcamp.capabilityservice.domain.model.Page;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.function.Function;

/**
 * Mapper para conversión de páginas de dominio a responses.
 */
@Mapper(componentModel = "spring")
public interface IPageMapper {

    /**
     * Convierte Page de dominio a PageResponse.
     * El contenido debe ser mapeado usando el mapper específico del tipo.
     */
    default <T, R> PageResponse<R> toResponse(Page<T> page, Function<List<T>, List<R>> contentMapper) {
        PageResponse<R> response = new PageResponse<>();
        response.setContent(contentMapper.apply(page.getContent()));
        response.setPageNumber(page.getPageNumber());
        response.setPageSize(page.getPageSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    /**
     * Convierte Page de dominio a PageResponse con contenido ya mapeado.
     */
    default <R> PageResponse<R> toResponse(Page<?> page, List<R> mappedContent) {
        PageResponse<R> response = new PageResponse<>();
        response.setContent(mappedContent);
        response.setPageNumber(page.getPageNumber());
        response.setPageSize(page.getPageSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}
