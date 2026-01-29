package com.bootcamp.capabilityservice.infrastructure.input.exception;

import com.bootcamp.capabilityservice.application.dto.response.ErrorResponse;
import com.bootcamp.capabilityservice.domain.exception.DomainException;
import com.bootcamp.capabilityservice.domain.exception.ExceptionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para WebFlux.
 * Implementa WebExceptionHandler para interceptar todas las excepciones.
 */
@Component
@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof DomainException domainException) {
            return handleDomainException(exchange, domainException);
        }
        
        if (ex instanceof WebExchangeBindException bindException) {
            return handleWebExchangeBindException(exchange, bindException);
        }
        
        return handleGenericException(exchange, ex);
    }

    private Mono<Void> handleDomainException(ServerWebExchange exchange, DomainException ex) {
        ExceptionResponse exceptionResponse = ex.getExceptionResponse();
        HttpStatus status = exceptionResponse.getStatus();
        
        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            exceptionResponse.getMessage(),
            ex.getAdditionalInfo()
        );
        
        log.warn("Domain exception: {} - {}", exceptionResponse.name(), ex.getMessage());
        
        return writeResponse(exchange, errorResponse, status);
    }

    private Mono<Void> handleWebExchangeBindException(ServerWebExchange exchange, WebExchangeBindException ex) {
        Map<String, String> fieldErrors = ex.getFieldErrors().stream()
            .collect(Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage() != null 
                    ? fieldError.getDefaultMessage() 
                    : "Invalid value",
                (existing, replacement) -> existing
            ));
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            fieldErrors
        );
        
        log.warn("Validation exception: {}", fieldErrors);
        
        return writeResponse(exchange, errorResponse, HttpStatus.BAD_REQUEST);
    }

    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("error", ex.getClass().getSimpleName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ExceptionResponse.INTERNAL_ERROR.getMessage(),
            additionalInfo
        );
        
        log.error("Unhandled exception: ", ex);
        
        return writeResponse(exchange, errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, ErrorResponse errorResponse, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return exchange.getResponse().setComplete();
        }
    }
}
