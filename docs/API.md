# Capability Service API Documentation

Documentación detallada de la API REST del Capability Service.

## Base URL

```
http://localhost:8082
```

## Autenticación

Actualmente no se requiere autenticación.

## Formato de Respuestas

Todas las respuestas están en formato JSON.

### Respuesta de Error

```json
{
  "status": 400,
  "message": "Capability must have between 3 and 20 technologies",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

### Respuesta Paginada

```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

---

## Capacidades

### POST /api/capabilities

Crear una nueva capacidad.

**Request Body:**
```json
{
  "name": "Backend Development",
  "description": "Tecnologías para desarrollo backend",
  "technologyIds": [1, 2, 3, 4]
}
```

**Validaciones:**
- `name`: Requerido, no vacío
- `description`: Requerido, no vacío
- `technologyIds`: Requerido, entre 3 y 20 elementos, sin duplicados

**Response 201:**
```json
{
  "id": 1,
  "name": "Backend Development",
  "description": "Tecnologías para desarrollo backend",
  "technologies": [
    {"id": 1, "name": "Java", "description": "Java programming language"},
    {"id": 2, "name": "Spring", "description": "Spring framework"},
    {"id": 3, "name": "MySQL", "description": "MySQL database"},
    {"id": 4, "name": "Docker", "description": "Container platform"}
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response 400:**
```json
{
  "status": 400,
  "message": "Capability must have between 3 and 20 technologies",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

### GET /api/capabilities

Listar capacidades con paginación.

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| page | integer | 0 | Número de página (0-indexed) |
| size | integer | 10 | Tamaño de página |
| sortBy | string | name | Campo de ordenamiento |
| direction | string | ASC | Dirección (ASC/DESC) |

**Ejemplo:**
```
GET /api/capabilities?page=0&size=10&sortBy=name&direction=ASC
```

**Response 200:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Backend Development",
      "description": "Tecnologías para desarrollo backend",
      "technologies": [
        {"id": 1, "name": "Java", "description": "Java programming language"}
      ],
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

### GET /api/capabilities/{id}

Obtener capacidad por ID.

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la capacidad |

**Response 200:**
```json
{
  "id": 1,
  "name": "Backend Development",
  "description": "Tecnologías para desarrollo backend",
  "technologies": [
    {"id": 1, "name": "Java", "description": "Java programming language"}
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response 404:**
```json
{
  "status": 404,
  "message": "Capability not found",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

### GET /api/capabilities/by-ids

Obtener múltiples capacidades por IDs.

**Query Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| ids | string | IDs separados por coma |

**Ejemplo:**
```
GET /api/capabilities/by-ids?ids=1,2,3
```

**Response 200:**
```json
[
  {
    "id": 1,
    "name": "Backend Development",
    "description": "Tecnologías backend",
    "technologies": [...],
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "name": "Frontend Development",
    "description": "Tecnologías frontend",
    "technologies": [...],
    "createdAt": "2024-01-15T11:00:00"
  }
]
```

---

### GET /api/capabilities/count-by-technology/{technologyId}

Contar capacidades que usan una tecnología específica.

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| technologyId | long | ID de la tecnología |

**Response 200:**
```json
5
```

---

### DELETE /api/capabilities/{id}

Eliminar una capacidad.

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la capacidad |

**Response 204:** Sin contenido

**Response 400:** (Capacidad en uso)
```json
{
  "status": 400,
  "message": "Cannot delete capability: it is being used by bootcamps",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

**Response 404:**
```json
{
  "status": 404,
  "message": "Capability not found",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

## Bootcamps

### POST /api/bootcamps

Crear un nuevo bootcamp (orquestación).

**Request Body:**
```json
{
  "name": "Full Stack Bootcamp",
  "description": "Bootcamp completo de desarrollo",
  "startDate": "2024-03-01",
  "endDate": "2024-06-01",
  "capabilityIds": [1, 2]
}
```

**Response 201:**
```json
{
  "id": 1,
  "name": "Full Stack Bootcamp",
  "description": "Bootcamp completo de desarrollo",
  "startDate": "2024-03-01",
  "endDate": "2024-06-01",
  "capabilities": [
    {
      "id": 1,
      "name": "Backend Development",
      "description": "Tecnologías backend",
      "technologies": [...],
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

**Response 404:**
```json
{
  "status": 404,
  "message": "Capability not found",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

### GET /api/bootcamps

Listar bootcamps con paginación.

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| page | integer | 0 | Número de página |
| size | integer | 10 | Tamaño de página |
| sortBy | string | name | Campo de ordenamiento |
| direction | string | ASC | Dirección (ASC/DESC) |

**Response 200:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Full Stack Bootcamp",
      "description": "Bootcamp completo",
      "startDate": "2024-03-01",
      "endDate": "2024-06-01",
      "capabilities": [...]
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

### DELETE /api/bootcamps/{id}

Eliminar bootcamp con Saga Pattern.

Este endpoint ejecuta una eliminación en cascada:
1. Elimina inscripciones del bootcamp (Person Service)
2. Elimina el bootcamp (Bootcamp Service)
3. Elimina capacidades huérfanas (si no están en uso)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del bootcamp |

**Response 204:** Sin contenido

**Response 503:**
```json
{
  "status": 503,
  "message": "External service unavailable",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

## Inscripciones

### POST /api/enrollments

Inscribir persona en bootcamp.

**Request Body:**
```json
{
  "bootcampId": 1,
  "personId": 1
}
```

**Response 201:** Sin contenido

**Response 400:**
```json
{
  "status": 400,
  "message": "Enrollment validation failed",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

## Reportes

### GET /api/reports/top-bootcamp

Obtener el bootcamp con más inscripciones.

**Response 200:**
```json
{
  "id": 1,
  "name": "Full Stack Bootcamp",
  "description": "Bootcamp más popular",
  "startDate": "2024-03-01",
  "endDate": "2024-06-01",
  "capabilities": [
    {
      "id": 1,
      "name": "Backend Development",
      "description": "Tecnologías backend",
      "technologies": [
        {"id": 1, "name": "Java", "description": "Java language"}
      ],
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

**Response 404:**
```json
{
  "status": 404,
  "message": "No bootcamps found",
  "timestamp": "2024-01-15T10:30:00",
  "additionalInfo": {}
}
```

---

## Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | OK - Operación exitosa |
| 201 | Created - Recurso creado |
| 204 | No Content - Eliminación exitosa |
| 400 | Bad Request - Error de validación |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error interno |
| 503 | Service Unavailable - Servicio externo no disponible |

---

## Ejemplos con cURL

### Crear capacidad
```bash
curl -X POST http://localhost:8082/api/capabilities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Backend Development",
    "description": "Tecnologías para desarrollo backend",
    "technologyIds": [1, 2, 3, 4]
  }'
```

### Listar capacidades
```bash
curl "http://localhost:8082/api/capabilities?page=0&size=10&sortBy=name&direction=ASC"
```

### Obtener capacidad por ID
```bash
curl http://localhost:8082/api/capabilities/1
```

### Eliminar capacidad
```bash
curl -X DELETE http://localhost:8082/api/capabilities/1
```

### Crear bootcamp
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

### Inscribir persona
```bash
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "bootcampId": 1,
    "personId": 1
  }'
```

### Obtener bootcamp más popular
```bash
curl http://localhost:8082/api/reports/top-bootcamp
```
