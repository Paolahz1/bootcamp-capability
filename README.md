# Capability Service

Microservicio orquestador por dominio para la gestión de capacidades y coordinación de bootcamps. Implementado con Spring WebFlux para procesamiento reactivo no bloqueante.

## Descripción

El Capability Service actúa como punto de entrada desde el frontend para operaciones relacionadas con:
- Gestión de capacidades (agrupaciones de 3-20 tecnologías)
- Orquestación de bootcamps (creación, listado, eliminación con Saga Pattern)
- Inscripciones de personas en bootcamps
- Reportes de popularidad

## Arquitectura

El servicio sigue **arquitectura hexagonal** con tres capas:
- **Domain**: Modelos, excepciones, puertos (API/SPI) y casos de uso
- **Application**: DTOs, mappers (MapStruct) y servicios de aplicación
- **Infrastructure**: Adaptadores REST, persistencia R2DBC y clientes WebClient

## Tecnologías

- Java 21
- Spring Boot 3.5.x
- Spring WebFlux (programación reactiva)
- R2DBC MySQL (persistencia reactiva)
- MapStruct (mapeo de objetos)
- SpringDoc OpenAPI (documentación)
- Testcontainers (tests de integración)
- jqwik (property-based testing)

## Requisitos Previos

- JDK 21+
- MySQL 8.0+
- Docker (para Testcontainers)

## Configuración de Base de Datos

Crear la base de datos y tablas:

```sql
CREATE DATABASE capability_db;
USE capability_db;

CREATE TABLE capabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
);

CREATE TABLE capability_technologies (
    capability_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    PRIMARY KEY (capability_id, technology_id),
    FOREIGN KEY (capability_id) REFERENCES capabilities(id) ON DELETE CASCADE,
    INDEX idx_technology_id (technology_id)
);
```

## Variables de Entorno

| Variable    | Descripción         | Valor por defecto |
| -------------| ---------------------| -------------------|
| DB_USERNAME | Usuario de MySQL    |                   |
| DB_PASSWORD | Contraseña de MySQL |                   |

## Ejecución

### Desarrollo Local

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun

# O con variables de entorno personalizadas
DB_USERNAME=myuser DB_PASSWORD=mypass ./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8082`

### Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Solo tests unitarios
./gradlew test --tests "*Test"

# Solo tests de integración
./gradlew test --tests "*IntegrationTest"
```

## Documentación API

Una vez iniciada la aplicación, acceder a:
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/api-docs

## Servicios Externos

El Capability Service se comunica con:

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Technology Service | 8081 | Gestión de tecnologías |
| Bootcamp Service | 8083 | Gestión de bootcamps |
| Person Service | 8084 | Gestión de personas e inscripciones |

## Endpoints

### Capacidades (`/api/capabilities`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/capabilities` | Crear capacidad |
| GET | `/api/capabilities` | Listar capacidades (paginado) |
| GET | `/api/capabilities/{id}` | Obtener capacidad por ID |
| GET | `/api/capabilities/by-ids?ids=1,2,3` | Obtener múltiples capacidades |
| GET | `/api/capabilities/count-by-technology/{technologyId}` | Contar capacidades por tecnología |
| DELETE | `/api/capabilities/{id}` | Eliminar capacidad |

### Bootcamps (`/api/bootcamps`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/bootcamps` | Crear bootcamp |
| GET | `/api/bootcamps` | Listar bootcamps (paginado) |
| DELETE | `/api/bootcamps/{id}` | Eliminar bootcamp (Saga Pattern) |

### Inscripciones (`/api/enrollments`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/enrollments` | Inscribir persona en bootcamp |

### Reportes (`/api/reports`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reports/top-bootcamp` | Obtener bootcamp más popular |

## Ejemplos de Uso

### Crear Capacidad

```bash
curl -X POST http://localhost:8082/api/capabilities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Backend Development",
    "description": "Tecnologías para desarrollo backend",
    "technologyIds": [1, 2, 3, 4]
  }'
```

### Listar Capacidades

```bash
curl "http://localhost:8082/api/capabilities?page=0&size=10&sortBy=name&direction=ASC"
```

### Crear Bootcamp

```bash
curl -X POST http://localhost:8082/api/bootcamps \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Full Stack Bootcamp",
    "description": "Bootcamp completo de desarrollo",
    "startDate": "2024-03-01",
    "endDate": "2024-06-01",
    "capabilityIds": [1, 2]
  }'
```

### Inscribir Persona

```bash
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "bootcampId": 1,
    "personId": 1
  }'
```

## Estructura del Proyecto

```
src/main/java/com/bootcamp/capabilityservice/
├── domain/
│   ├── model/          # Modelos de dominio
│   ├── exception/      # Excepciones de dominio
│   ├── api/            # Puertos de entrada
│   ├── spi/            # Puertos de salida
│   └── usecase/        # Casos de uso
├── application/
│   ├── dto/            # DTOs request/response
│   ├── mapper/         # Mappers MapStruct
│   └── service/        # Servicios de aplicación
└── infrastructure/
    ├── config/         # Configuración
    ├── input/rest/     # Handlers y routers
    └── output/         # Adaptadores de persistencia y clientes
```

