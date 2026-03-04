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

Environment variables required locally (see `.env`): `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` pointing to a PostgreSQL instance.

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
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java
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

### Database Migrations

Schema changes go in `src/main/resources/db/changelog/scripts/` as SQL files and must be referenced in `db.changelog-master.xml`. Liquibase is disabled in the test profile; H2 auto-creates the schema via `ddl-auto=create-drop`.

### Key Conventions

- UUID primary keys on all entities (e.g., `community_key`, `resident_key`).
- Soft deletes via `active` boolean field on community and resident entities.
- JPA Specifications (`*Specs.java`) used for dynamic filtering in paginated endpoints.
- Mappers (`*AppMapper.java`) in `business/<module>/mapper/` handle all entity ↔ DTO conversion — never map inside controllers or use cases directly.
- Validation errors return structured responses via `GlobalExceptionHandler` using `MethodArgumentNotValidException` and `ConstraintViolationException`.
- CORS is configured in `CorsConfig.java` to allow `localhost:4200` (Angular frontend).