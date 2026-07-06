# Parking Management System - Autorización con AWS Cognito

Repositorio: https://github.com/Aguilar2605Developer/ae_2026_01_1463_Julian_Solorzano_roles

## Descripción

Sistema de gestión de estacionamiento con autorización basada en roles usando AWS Cognito. Solo administradores pueden crear espacios; usuarios normales pueden registrar entrada y salida de vehículos.

## Tecnología

- Kotlin 2.3.21
- Spring Boot 4.1.0
- Java 21
- PostgreSQL (docker-compose)
- OAuth2 Resource Server (Cognito)
- Gradle Kotlin DSL

## Endpoints

| Método | Ruta | Acceso | Rol |
|---|---|---|---|
| GET | `/parking-spaces/available` | Público | ninguno |
| POST | `/parking-spaces` | Privado | ADMIN |
| POST | `/tickets/entry` | Privado | USER |
| POST | `/tickets/exit` | Privado | USER |


La aplicación estará disponible en `http://localhost:8080`.

## Seguridad

- Los roles (ADMIN, USER) vienen en el JWT de Cognito (`cognito:groups`).
- Se mapean a `ROLE_ADMIN` y `ROLE_USER` en Spring Security.
- Sin token: 401 Unauthorized.
- Con token pero sin rol correcto: 403 Forbidden.

## Estructura

```
src/main/kotlin/com/pucetec/roles/
├── config/           SecurityConfig (mapeo de roles)
├── controllers/      ParkingSpacesController, TicketsController
├── services/         EstacionamientoService
├── repositories/     EspacioRepository, TicketRepository
├── entities/         Espacio, Ticket
├── dto/              DTOs (separados de entities)
├── mappers/          EspacioMapper, TicketMapper
├── exceptions/       Excepciones personalizadas + handler global
```

## Pruebas

- 13 tests unitarios del service (100% cobertura línea y rama).
- 5 tests de seguridad/web para validar autorización por rol.

## Evidencia

Carpeta `evidence/` contiene:
- Capturas de Cognito (User Pool, grupos, membresías).
- Tokens decodificados en jwt.io (mostrando `cognito:groups`).
- Requests de Postman (401, 403, éxitos).
- Colección de Postman exportada.
- Reporte de cobertura de tests.

## Base de datos

Dos tablas:
- `Espacio`: código y disponibilidad.
- `Ticket`: placa, fechas de entrada/salida, referencia al espacio.

Capacidad máxima configurable como variable del servicio (20 espacios).
