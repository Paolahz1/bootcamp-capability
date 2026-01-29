package com.bootcamp.capabilityservice.infrastructure.input.rest.handler;

import com.bootcamp.capabilityservice.application.service.ReportApplicationService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler para endpoints de reportes.
 */
@Component
public class ReportHandler {

    private final ReportApplicationService reportService;

    public ReportHandler(ReportApplicationService reportService) {
        this.reportService = reportService;
    }

    public Mono<ServerResponse> getTopBootcamp(ServerRequest request) {
        return reportService.getTopBootcamp()
            .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }
}
