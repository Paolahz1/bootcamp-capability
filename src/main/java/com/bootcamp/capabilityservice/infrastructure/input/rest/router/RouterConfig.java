package com.bootcamp.capabilityservice.infrastructure.input.rest.router;

import com.bootcamp.capabilityservice.application.dto.request.CreateBootcampRequest;
import com.bootcamp.capabilityservice.application.dto.request.CreateCapabilityRequest;
import com.bootcamp.capabilityservice.application.dto.request.EnrollmentRequest;
import com.bootcamp.capabilityservice.application.dto.response.*;
import com.bootcamp.capabilityservice.infrastructure.input.rest.handler.BootcampHandler;
import com.bootcamp.capabilityservice.infrastructure.input.rest.handler.CapabilityHandler;
import com.bootcamp.capabilityservice.infrastructure.input.rest.handler.ReportHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Configuración de rutas usando RouterFunctions con documentación OpenAPI.
 */
@Configuration
public class RouterConfig {

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/capabilities",
            method = RequestMethod.POST,
            beanClass = CapabilityHandler.class,
            beanMethod = "createCapability",
            operation = @Operation(
                operationId = "createCapability",
                summary = "Crear una nueva capacidad",
                tags = {"Capabilities"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCapabilityRequest.class))
                ),
                responses = {
                    @ApiResponse(responseCode = "201", description = "Capacidad creada exitosamente",
                        content = @Content(schema = @Schema(implementation = CapabilityResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        ),
        @RouterOperation(
            path = "/api/capabilities",
            method = RequestMethod.GET,
            beanClass = CapabilityHandler.class,
            beanMethod = "listCapabilities",
            operation = @Operation(
                operationId = "listCapabilities",
                summary = "Listar capacidades con paginación",
                tags = {"Capabilities"},
                parameters = {
                    @Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "name")),
                    @Parameter(name = "direction", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "ASC"))
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de capacidades",
                        content = @Content(schema = @Schema(implementation = PageResponse.class)))
                }
            )
        ),
        @RouterOperation(
            path = "/api/capabilities/by-ids",
            method = RequestMethod.GET,
            beanClass = CapabilityHandler.class,
            beanMethod = "getCapabilitiesByIds",
            operation = @Operation(
                operationId = "getCapabilitiesByIds",
                summary = "Obtener múltiples capacidades por IDs",
                tags = {"Capabilities"},
                parameters = {
                    @Parameter(name = "ids", in = ParameterIn.QUERY, required = true, 
                        schema = @Schema(type = "string"), description = "IDs separados por coma")
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de capacidades encontradas",
                        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityResponse.class))))
                }
            )
        ),
        @RouterOperation(
            path = "/api/capabilities/count-by-technology/{technologyId}",
            method = RequestMethod.GET,
            beanClass = CapabilityHandler.class,
            beanMethod = "countByTechnology",
            operation = @Operation(
                operationId = "countByTechnology",
                summary = "Contar capacidades por tecnología",
                tags = {"Capabilities"},
                parameters = {
                    @Parameter(name = "technologyId", in = ParameterIn.PATH, required = true, 
                        schema = @Schema(type = "integer", format = "int64"))
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "Conteo de capacidades",
                        content = @Content(schema = @Schema(type = "integer", format = "int64")))
                }
            )
        ),
        @RouterOperation(
            path = "/api/capabilities/{id}",
            method = RequestMethod.GET,
            beanClass = CapabilityHandler.class,
            beanMethod = "getCapabilityById",
            operation = @Operation(
                operationId = "getCapabilityById",
                summary = "Obtener capacidad por ID",
                tags = {"Capabilities"},
                parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, required = true, 
                        schema = @Schema(type = "integer", format = "int64"))
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "Capacidad encontrada",
                        content = @Content(schema = @Schema(implementation = CapabilityResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Capacidad no encontrada",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        ),
        @RouterOperation(
            path = "/api/capabilities/{id}",
            method = RequestMethod.DELETE,
            beanClass = CapabilityHandler.class,
            beanMethod = "deleteCapability",
            operation = @Operation(
                operationId = "deleteCapability",
                summary = "Eliminar capacidad",
                tags = {"Capabilities"},
                parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, required = true, 
                        schema = @Schema(type = "integer", format = "int64"))
                },
                responses = {
                    @ApiResponse(responseCode = "204", description = "Capacidad eliminada"),
                    @ApiResponse(responseCode = "400", description = "Capacidad en uso",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Capacidad no encontrada",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        )
    })
    public RouterFunction<ServerResponse> capabilityRoutes(CapabilityHandler handler) {
        return RouterFunctions
            .route(POST("/api/capabilities"), handler::createCapability)
            .andRoute(GET("/api/capabilities"), handler::listCapabilities)
            .andRoute(GET("/api/capabilities/by-ids"), handler::getCapabilitiesByIds)
            .andRoute(GET("/api/capabilities/count-by-technology/{technologyId}"), handler::countByTechnology)
            .andRoute(GET("/api/capabilities/{id}"), handler::getCapabilityById)
            .andRoute(DELETE("/api/capabilities/{id}"), handler::deleteCapability);
    }

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/bootcamps",
            method = RequestMethod.POST,
            beanClass = BootcampHandler.class,
            beanMethod = "createBootcamp",
            operation = @Operation(
                operationId = "createBootcamp",
                summary = "Crear un nuevo bootcamp",
                tags = {"Bootcamps"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateBootcampRequest.class))
                ),
                responses = {
                    @ApiResponse(responseCode = "201", description = "Bootcamp creado exitosamente",
                        content = @Content(schema = @Schema(implementation = BootcampResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Capacidad no encontrada",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        ),
        @RouterOperation(
            path = "/api/bootcamps",
            method = RequestMethod.GET,
            beanClass = BootcampHandler.class,
            beanMethod = "listBootcamps",
            operation = @Operation(
                operationId = "listBootcamps",
                summary = "Listar bootcamps con paginación",
                tags = {"Bootcamps"},
                parameters = {
                    @Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "name")),
                    @Parameter(name = "direction", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "ASC"))
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de bootcamps",
                        content = @Content(schema = @Schema(implementation = PageResponse.class)))
                }
            )
        ),
        @RouterOperation(
            path = "/api/bootcamps/{id}",
            method = RequestMethod.DELETE,
            beanClass = BootcampHandler.class,
            beanMethod = "deleteBootcamp",
            operation = @Operation(
                operationId = "deleteBootcamp",
                summary = "Eliminar bootcamp con Saga Pattern",
                tags = {"Bootcamps"},
                parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, required = true, 
                        schema = @Schema(type = "integer", format = "int64"))
                },
                responses = {
                    @ApiResponse(responseCode = "204", description = "Bootcamp eliminado"),
                    @ApiResponse(responseCode = "503", description = "Error en servicio externo",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        ),
        @RouterOperation(
            path = "/api/enrollments",
            method = RequestMethod.POST,
            beanClass = BootcampHandler.class,
            beanMethod = "enrollPerson",
            operation = @Operation(
                operationId = "enrollPerson",
                summary = "Inscribir persona en bootcamp",
                tags = {"Enrollments"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = EnrollmentRequest.class))
                ),
                responses = {
                    @ApiResponse(responseCode = "201", description = "Inscripción exitosa"),
                    @ApiResponse(responseCode = "400", description = "Error de validación",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        )
    })
    public RouterFunction<ServerResponse> bootcampRoutes(BootcampHandler handler) {
        return RouterFunctions
            .route(POST("/api/bootcamps"), handler::createBootcamp)
            .andRoute(GET("/api/bootcamps"), handler::listBootcamps)
            .andRoute(DELETE("/api/bootcamps/{id}"), handler::deleteBootcamp)
            .andRoute(POST("/api/enrollments"), handler::enrollPerson);
    }

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/reports/top-bootcamp",
            method = RequestMethod.GET,
            beanClass = ReportHandler.class,
            beanMethod = "getTopBootcamp",
            operation = @Operation(
                operationId = "getTopBootcamp",
                summary = "Obtener bootcamp con más inscripciones",
                tags = {"Reports"},
                responses = {
                    @ApiResponse(responseCode = "200", description = "Bootcamp más popular",
                        content = @Content(schema = @Schema(implementation = BootcampResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No hay bootcamps",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
            )
        )
    })
    public RouterFunction<ServerResponse> reportRoutes(ReportHandler handler) {
        return RouterFunctions
            .route(GET("/api/reports/top-bootcamp"), handler::getTopBootcamp);
    }
}
