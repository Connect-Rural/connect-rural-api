# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package           # Compile and package JAR
mvn clean verify            # Build with all tests

# Run
mvn spring-boot:run         # Start dev server on port 8080

# Test
mvn test                    # Run all tests
mvn test -Dtest=ClassName   # Run a single test class

# Docker
docker-compose up --build   # Build and start container
```

Environment variables required locally (see `.env`): `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` pointing to a PostgreSQL instance. Also `WHATSAPP_GATEWAY_URL` (URL del gateway, default `http://localhost:8081`) y `APP_BASE_URL` (URL pública de este servicio, default `http://localhost:8080`) para la integración WhatsApp. El `appKey` por comunidad se almacena en `community.whatsappAppKey` en BD — no hay una variable global de tenant.

Tests use an H2 in-memory database with profile `test` (see `src/test/resources/application-test.properties`).

## Project Structure

```
connect-rural-api/
├── src/
│   ├── main/
│   │   ├── java/com/crdev/connect_rural_api/
│   │   │   ├── ConnectRuralApiApplication.java
│   │   │   ├── app/
│   │   │   │   ├── community/
│   │   │   │   │   ├── CommunityController.java
│   │   │   │   │   └── dto/{request,response}/
│   │   │   │   ├── cooperation/
│   │   │   │   │   ├── CooperationController.java
│   │   │   │   │   └── dto/{request,response}/
│   │   │   │   ├── resident/
│   │   │   │   │   ├── ResidentController.java
│   │   │   │   │   └── dto/{request,response}/
│   │   │   │   ├── controllers/
│   │   │   │   │   └── HealthController.java
│   │   │   │   └── exceptions/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── business/
│   │   │   │   ├── community/
│   │   │   │   │   ├── CommunityService.java
│   │   │   │   │   ├── mapper/CommunityAppMapper.java
│   │   │   │   │   └── usecases/          ← Create,Delete,Get*,Update use cases
│   │   │   │   ├── cooperation/
│   │   │   │   │   ├── CooperationService.java
│   │   │   │   │   ├── enums/CooperationAssignmentType.java
│   │   │   │   │   ├── mapper/CooperationAppMapper.java
│   │   │   │   │   ├── specs/CooperationSpecs.java
│   │   │   │   │   └── usecases/
│   │   │   │   ├── cooperationResident/
│   │   │   │   │   └── CooperationResidentService.java
│   │   │   │   └── resident/
│   │   │   │       ├── ResidentService.java
│   │   │   │       ├── mapper/ResidentAppMapper.java
│   │   │   │       ├── specs/ResidentSpecs.java
│   │   │   │       └── usecases/
│   │   │   │   ├── whatsapp/
│   │   │   │   │   ├── WhatsappGatewayCallbackController.java  ← recibe eventos del gateway
│   │   │   │   │   └── dto/request/   ← GatewayEventDto, GatewayMessageDto, GatewayStatusDto,
│   │   │   │   │                          SendWhatsappMessageDto
│   │   │   ├── business/
│   │   │   │   ├── whatsapp/
│   │   │   │   │   ├── WhatsappGatewayService.java  ← envía mensajes al gateway via HTTP
│   │   │   │   │   └── usecases/      ← ProcessGatewayEventUseCase, SendWhatsappMessageUseCase
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── WhatsappGatewayConfig.java  ← RestClient con X-API-Key
│   │   │   └── data/
│   │   │       ├── community/    ← CommunityEntity, CommunityRepository
│   │   │       ├── cooperation/  ← CooperationEntity, CooperationRepository
│   │   │       ├── cooperationResident/ ← CooperationResidentEntity, Repository
│   │   │       └── resident/     ← ResidentEntity, ResidentRepository, SimpleResident
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/
│   │           ├── db.changelog-master.xml
│   │           └── scripts/      ← SQL migration files
│   └── test/
│       ├── java/.../ConnectRuralApiApplicationTests.java
│       └── resources/application-test.properties
├── .env                          ← Local DB credentials (not committed)
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Architecture

**Stack**: Spring Boot 3.5 · Java 21 · PostgreSQL · Liquibase · Lombok

The app follows a strict **3-layer architecture** enforced by package naming:

```
com.crdev.connect_rural_api/
  app/        ← Controllers + Request/Response DTOs + GlobalExceptionHandler
  business/   ← Use Cases + Services + Mappers + Specs + Enums
  data/       ← JPA Entities + Repositories
  config/     ← CORS and Spring configuration beans
```

### Use Case Pattern

Each CRUD operation lives in its own `@Component` class with a single `execute(...)` method (e.g., `CreateCooperationUseCase`, `GetCooperationDetailByKeyUseCase`). Controllers inject all relevant use cases via `@RequiredArgsConstructor`. Services handle repository access; use cases orchestrate business logic across multiple services.

### Domain Modules

All domain modules follow the same internal structure under both `app/` and `business/`:

- **community** – Top-level entity. Routes: `/api/communities`
- **resident** – Scoped to a community. Routes: `/api/{communityKey}/residents`
- **cooperation** – Payment collection system scoped to a community. Routes: `/api/{communityKey}/cooperations`
- **cooperationResident** – Junction table tracking resident ↔ cooperation assignments and payment status. Managed only via `CooperationResidentService`; no dedicated controller.
- **whatsapp** – Integración con `whatsapp-gateway`. Recibe eventos normalizados en `POST /api/whatsapp/events` y envía mensajes vía `WhatsappGatewayService`. No tiene capa `data/` propia.

### WhatsApp Gateway Integration

Cada **comunidad** puede ser un tenant independiente en el `whatsapp-gateway` (una cuenta de WhatsApp Business distinta por comunidad). El `tenantKey` del gateway se almacena en `community.whatsappAppKey`.

**Flujo entrante** (Meta → gateway → connect-rural-api):
1. Gateway llama `POST /api/whatsapp/events` con `GatewayEventDto`
2. `ProcessGatewayEventUseCase` resuelve la comunidad por `whatsappAppKey = event.tenantKey`
3. Aplica lógica de negocio de esa comunidad

**Flujo saliente** (connect-rural-api → gateway → Meta):
1. `SendWhatsappMessageUseCase.execute(appKey, to, text)` → `WhatsappGatewayService`
2. `POST {gateway}/api/messages/send?appKey={communityWhatsappAppKey}`

**Registro de tenant** (administración):
- `POST /api/communities/{key}/whatsapp` + `{phoneNumberId, accessToken}` → llama `POST {gateway}/api/tenants`, guarda appKey en la comunidad
- `DELETE /api/communities/{key}/whatsapp` → elimina tenant del gateway y limpia appKey

**Notificación a residente**:
- `NotifyResidentUseCase.execute(communityKey, residentKey, message)` — requiere `community.whatsappAppKey` y `resident.phoneNumber`

**Use cases WhatsApp** en `business/whatsapp/usecases/`:
- `ProcessGatewayEventUseCase` — procesa eventos entrantes del gateway
- `SendWhatsappMessageUseCase` — envía mensajes vía gateway
- `RegisterCommunityTenantUseCase` — registra comunidad como tenant en el gateway
- `UnlinkCommunityTenantUseCase` — desvincula comunidad del gateway
- `NotifyResidentUseCase` — envía mensaje a un residente por WhatsApp

### Database Migrations

Schema changes go in `src/main/resources/db/changelog/scripts/` as SQL files and must be referenced in `db.changelog-master.xml`. Liquibase is disabled in the test profile; H2 auto-creates the schema via `ddl-auto=create-drop`.

### Key Conventions

- UUID primary keys on all entities (e.g., `community_key`, `resident_key`).
- Soft deletes via `active` boolean field on community and resident entities.
- JPA Specifications (`*Specs.java`) used for dynamic filtering in paginated endpoints.
- Mappers (`*AppMapper.java`) in `business/<module>/mapper/` handle all entity ↔ DTO conversion — never map inside controllers or use cases directly.
- Validation errors return structured responses via `GlobalExceptionHandler` using `MethodArgumentNotValidException` and `ConstraintViolationException`.
- CORS is configured in `CorsConfig.java` to allow `localhost:4200` (Angular frontend).