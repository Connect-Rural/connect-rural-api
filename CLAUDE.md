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

Current local DB target is `cr-development`; application tables run under schema `connect_rural`. UUID defaults must use `public.uuid_generate_v4()` (schema-qualified).

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
│   │   │   │   │   └── dto/                       ← Request + Response DTOs
│   │   │   │   ├── resident/
│   │   │   │   │   ├── ResidentController.java
│   │   │   │   │   └── dto/
│   │   │   │   ├── cooperation/
│   │   │   │   │   ├── CooperationController.java
│   │   │   │   │   └── dto/
│   │   │   │   ├── file/
│   │   │   │   │   ├── FileController.java
│   │   │   │   │   └── dto/
│   │   │   │   ├── whatsapp/
│   │   │   │   │   ├── WhatsappCallbackController.java
│   │   │   │   │   └── dto/
│   │   │   │   └── shared/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── business/
│   │   │   │   ├── community/
│   │   │   │   │   ├── CommunityService.java
│   │   │   │   │   ├── CommunityRepository.java   ← interfaz (puerto BD)
│   │   │   │   │   └── dto/
│   │   │   │   ├── resident/
│   │   │   │   │   ├── ResidentService.java
│   │   │   │   │   ├── ResidentRepository.java
│   │   │   │   │   └── dto/
│   │   │   │   ├── cooperation/
│   │   │   │   │   ├── CooperationService.java
│   │   │   │   │   ├── CooperationRepository.java
│   │   │   │   │   ├── CooperationAssignmentType.java
│   │   │   │   │   ├── LateFeeCalculator.java
│   │   │   │   │   └── dto/
│   │   │   │   ├── cooperationresident/
│   │   │   │   │   ├── CooperationResidentService.java
│   │   │   │   │   └── CooperationResidentRepository.java
│   │   │   │   ├── financialobligation/
│   │   │   │   │   ├── FinancialObligationService.java
│   │   │   │   │   └── FinancialObligationRepository.java
│   │   │   │   ├── residentpayment/
│   │   │   │   │   ├── ResidentPaymentService.java
│   │   │   │   │   └── ResidentPaymentRepository.java
│   │   │   │   ├── file/
│   │   │   │   │   ├── FileService.java
│   │   │   │   │   ├── FileRepository.java
│   │   │   │   │   ├── FileGateway.java           ← interfaz (puerto MinIO)
│   │   │   │   │   └── dto/
│   │   │   │   └── whatsapp/
│   │   │   │       ├── WhatsappService.java
│   │   │   │       └── WhatsappGateway.java       ← interfaz (puerto gateway HTTP)
│   │   │   ├── data/
│   │   │   │   ├── community/
│   │   │   │   │   ├── CommunityRepositoryImpl.java
│   │   │   │   │   ├── CommunityJpaRepository.java
│   │   │   │   │   ├── CommunityEntity.java
│   │   │   │   │   └── CommunitySpecs.java
│   │   │   │   ├── resident/
│   │   │   │   ├── cooperation/
│   │   │   │   ├── cooperationresident/
│   │   │   │   ├── financialobligation/
│   │   │   │   ├── residentpayment/
│   │   │   │   ├── paymentallocation/
│   │   │   │   ├── file/
│   │   │   │   │   ├── FileRepositoryImpl.java
│   │   │   │   │   ├── FileGatewayImpl.java       ← MinIO client
│   │   │   │   │   ├── FileJpaRepository.java
│   │   │   │   │   ├── FileObjectEntity.java
│   │   │   │   │   └── FileStatus.java
│   │   │   │   └── whatsapp/
│   │   │   │       └── WhatsappGatewayImpl.java   ← RestClient HTTP
│   │   │   └── config/
│   │   │       ├── CorsConfig.java
│   │   │       ├── MinioConfig.java
│   │   │       └── WhatsappGatewayConfig.java
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

**Pattern**: Hexagonal Architecture (Ports & Adapters) in 3 layers. Full details in [`docs/architecture.md`](docs/architecture.md).

```
app/        ← HTTP adapters: Controllers + Request/Response DTOs
business/   ← Domain: Services + Repository interfaces + Gateway interfaces + enums
data/       ← Infrastructure: RepositoryImpl + JpaRepository + GatewayImpl + Entities + Specs
config/     ← Spring configuration beans
```

### Dependency Rules

```
Controller → Service                          ✓
Service    → Repository (any module)          ✓
Service    → Gateway                          ✓
Service    → Service (another module)         ✗  never
business   → data (direct import)             ✗  never
```

### Naming Conventions

| Artifact | Pattern | Example |
|---|---|---|
| Controller | `{Module}Controller` | `CooperationController` |
| Service | `{Module}Service` | `CooperationService` |
| Repository interface | `{Module}Repository` in `business/` | `CooperationRepository` |
| Gateway interface | `{Module}Gateway` in `business/` | `WhatsappGateway` |
| Repository impl | `{Module}RepositoryImpl` in `data/` | `CooperationRepositoryImpl` |
| Gateway impl | `{Module}GatewayImpl` in `data/` | `WhatsappGatewayImpl` |
| Spring Data | `{Module}JpaRepository` in `data/` | `CooperationJpaRepository` |
| JPA Entity | `{Module}Entity` | `CooperationEntity` |
| JPA Specs | `{Module}Specs` in `data/` | `CooperationSpecs` |
| Request DTO | `{Action}{Module}Request` | `CreateCooperationRequest` |
| Response DTO | `{Module}Response` | `CooperationResponse` |
| Paginated response | `{Module}PageResponse` | `CooperationPageResponse` |
| Package names | lowercase, no camelCase | `cooperationresident/` |

### Domain Modules

- **community** — top-level entity. Routes: `/api/communities`
- **resident** — scoped to a community. Routes: `/api/{communityKey}/residents`
- **cooperation** — payment collection scoped to a community. Routes: `/api/{communityKey}/cooperations`
- **cooperationresident** — junction table resident ↔ cooperation; no controller. Payment endpoints live under `/api/{communityKey}/cooperations/{cooperationKey}/residents/`
- **financialobligation** — financial obligations; no controller
- **residentpayment** — payment records; no controller
- **file** — file metadata + MinIO storage. Routes: `/api/files`
- **whatsapp** — integration with external whatsapp-gateway. Callback: `POST /api/whatsapp/events`

### WhatsApp Gateway Integration

Cada **comunidad** puede ser un tenant independiente en el `whatsapp-gateway`. El `tenantKey` se almacena en `community.whatsappAppKey`.

**Flujo entrante** (Meta → gateway → connect-rural-api):
1. Gateway llama `POST /api/whatsapp/events`
2. `WhatsappService` resuelve la comunidad por `whatsappAppKey = event.tenantKey`

**Flujo saliente** (connect-rural-api → gateway → Meta):
1. `WhatsappService` llama `WhatsappGateway` con `appKey`, destinatario y texto
2. `WhatsappGatewayImpl` hace `POST {gateway}/api/messages/send?appKey={appKey}`

**Registro de tenant**:
- `POST /api/communities/{key}/whatsapp` → registra comunidad en el gateway, guarda `appKey`
- `DELETE /api/communities/{key}/whatsapp` → elimina tenant del gateway, limpia `appKey`

### Cooperation Payment Flow

`GET /{cooperationKey}/detail` returns a list of assigned residents with computed fields:
- `paymentStatus`: `PAGADO` | `VENCIDO` (unpaid & past dueDate) | `PENDIENTE`
- `baseAmount`, `lateFeeAmount` (only when `VENCIDO`), `totalAmount`
- `amountPaid`, `paidAt`

Payment operations on `CooperationService`:
- `markAsPaid(cooperationKey, residentKey, paidAt?, amountPaid?)` — `PATCH .../residents/{residentKey}/pay`
- `markAsUnpaid(cooperationKey, residentKey)` — `PATCH .../residents/{residentKey}/unpay`
- `markAllAsPaid(cooperationKey)` — `PATCH .../residents/pay-all` — returns `{ updated: N }`

### Cooperation Status Flow

Cooperations have `status`: `OPEN` (default) or `CLOSED`.

- `close(cooperationKey)` — `PATCH /api/{communityKey}/cooperations/{cooperationKey}/close` — sets `status=CLOSED` and records `closedAt`; throws `IllegalStateException` if already closed.

### Database Migrations

Schema changes go in `src/main/resources/db/changelog/scripts/` as SQL files and must be referenced in `db.changelog-master.xml`. Liquibase is disabled in the test profile; H2 auto-creates the schema via `ddl-auto=create-drop`.

All SQL migrations must use schema-qualified table names (`connect_rural.<table>`) and `public.uuid_generate_v4()` for UUID defaults. Unqualified references will break in production.

### Key Conventions

- UUID primary keys on all entities (`community_key`, `resident_key`, etc.)
- Soft deletes via `active` boolean on community and resident entities
- `{Module}Specs` in `data/` handle dynamic JPA filtering for paginated endpoints
- Mappers live in `business/{module}/` as `{Module}Mapper` — they convert between entities and DTOs
- `paymentallocation` module only has Repository interface/impl; no Service (accessed directly by `ResidentPaymentService`)
- Validation errors return structured responses via `GlobalExceptionHandler` using `MethodArgumentNotValidException` and `ConstraintViolationException`
- CORS configured in `CorsConfig.java` to allow `localhost:4200` (Angular frontend)
