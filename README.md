# ms-courses

Microservicio responsable del catálogo de asignaturas y del control estricto de cupos académicos en el sistema UniRemington.

## Puerto

`8081`

## Cómo ejecutar

> Requiere que eureka-server esté corriendo primero.

```bash
mvn spring-boot:run
```

O ejecutar `MsCoursesApplication.java` desde el IDE.

## Swagger UI

```
http://localhost:8081/swagger-ui/index.html
```

## Endpoints

| Método | URL | Descripción | HTTP |
|---|---|---|---|
| GET | `/api/courses` | Listar todos los cursos | 200 |
| GET | `/api/courses/{id}` | Obtener curso por ID | 200 / 404 |
| POST | `/api/courses` | Crear curso | 200 |
| PUT | `/api/courses/{id}` | Actualizar curso | 200 / 404 |
| DELETE | `/api/courses/{id}` | Eliminar curso | 200 / 404 |
| PUT | `/api/courses/{id}/decrease-quota` | Reservar un cupo (usado por ms-students) | 200 / 404 / 409 |
| PUT | `/api/courses/{id}/increase-quota` | Liberar un cupo (usado por ms-students) | 200 / 404 |

## Modelo

```json
{
  "id": 1,
  "name": "Programacion I",
  "credits": 3,
  "availableQuotas": 30
}
```

## Excepciones de negocio

| Excepción | Código HTTP | Descripción |
|---|---|---|
| `CourseNotFoundException` | 404 | El curso no existe |
| `NoAvailableQuotasException` | 409 | El curso no tiene cupos disponibles |

## Pruebas

```bash
mvn verify
```

Ejecuta pruebas unitarias (JUnit 5 + Mockito) y valida cobertura Jacoco ≥ 80%.

- `CourseServiceTest` — lógica de negocio pura, sin contexto Spring
- `CourseControllerTest` — endpoints con MockMvc standalone

## Stack

- Spring Boot 4.0.6 / Spring Cloud 2025.1.1
- Spring Data JPA + H2 (en memoria, se reinicia con el servicio)
- springdoc-openapi 3.0.3
- Jacoco 0.8.12
