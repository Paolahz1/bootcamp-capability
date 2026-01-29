package com.bootcamp.capabilityservice.infrastructure.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuración de WebClient para comunicación con servicios externos.
 */
@Configuration
public class WebClientConfiguration {

    @Bean("technologyWebClient")
    public WebClient technologyWebClient(
            @Value("${external-services.technology.base-url}") String baseUrl,
            @Value("${external-services.technology.timeout:5s}") Duration timeout) {
        return createWebClient(baseUrl, timeout);
    }

    @Bean("bootcampWebClient")
    public WebClient bootcampWebClient(
            @Value("${external-services.bootcamp.base-url}") String baseUrl,
            @Value("${external-services.bootcamp.timeout:5s}") Duration timeout) {
        return createWebClient(baseUrl, timeout);
    }

    @Bean("personWebClient")
    public WebClient personWebClient(
            @Value("${external-services.person.base-url}") String baseUrl,
            @Value("${external-services.person.timeout:5s}") Duration timeout) {
        return createWebClient(baseUrl, timeout);
    }

    private WebClient createWebClient(String baseUrl, Duration timeout) {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(timeout)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
