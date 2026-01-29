package com.bootcamp.capabilityservice;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base abstracta para tests de integración.
 * Configura un contenedor MySQL usando Testcontainers para proporcionar
 * una base de datos real durante los tests.
 * 
 * Los tests que extiendan esta clase tendrán acceso a:
 * - Una instancia de MySQL real ejecutándose en un contenedor Docker
 * - Configuración automática de R2DBC para conectarse al contenedor
 * - WebTestClient para probar endpoints REST
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String DATABASE_NAME = "capability_db";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";

    @Container
    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(MYSQL_IMAGE)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withInitScript("schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format(
                "r2dbc:mysql://%s:%d/%s",
                mysqlContainer.getHost(),
                mysqlContainer.getMappedPort(3306),
                DATABASE_NAME
        ));
        registry.add("spring.r2dbc.username", () -> USERNAME);
        registry.add("spring.r2dbc.password", () -> PASSWORD);
    }

    @BeforeAll
    static void beforeAll() {
        mysqlContainer.start();
    }
}
