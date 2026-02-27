# CLAUDE.md — Connect Rural API

This document provides guidance for AI assistants working on the **connect-rural-api** codebase. It covers architecture, conventions, workflows, and commands needed to be productive.

---

## Project Overview

**Connect Rural API** is a Spring Boot REST API for managing rural communities (`communities`) and their registered residents (`residents`). It follows a clean, layered architecture and uses PostgreSQL in production with Liquibase for schema migrations.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.0 |
| ORM | Spring Data JPA (Hibernate) |
| Database (prod) | PostgreSQL |
| Database (test) | H2 in-memory |
| Migrations | Liquibase |
| Build tool | Maven 3.9.6 (via wrapper) |
| Code generation | Lombok |
| Containerization | Docker + Docker Compose |
| Validation | Jakarta Validation API (Bean Validation) |

---

## Repository Structure

```
connect-rural-api/
├── src/
│   ├── main/
│   │   ├── java/com/crdev/connect_rural_api/
│   │   │   ├── app/                        # Presentation layer (REST)
│   │   │   │   ├── community/              # Community controller, request/response DTOs
│   │   │   │   ├── resident/               # Resident controller, request/response DTOs
│   │   │   │   ├── controllers/            # Other controllers (e.g., HealthController)
│   │   │   │   └── exceptions/             # GlobalExceptionHandler
│   │   │   ├── business/                   # Business logic layer
│   │   │   │   ├── community/
│   │   │   │   │   ├── usecases/           # One class per use case
│   │   │   │   │   └── mapper/             # CommunityAppMapper (DTO <-> Entity)
│   │   │   │   └── resident/
│   │   │   │       ├── usecases/           # One class per use case
│   │   │   │       ├── mapper/             # ResidentAppMapper
│   │   │   │       └── specs/              # JPA Specifications for dynamic filtering
│   │   │   ├── data/                       # Data access layer
│   │   │   │   ├── community/              # CommunityEntity, CommunityRepository
│   │   │   │   └── resident/               # ResidentEntity, ResidentRepository
│   │   │   ├── config/                     # Spring configuration (CORS, etc.)
│   │   │   └── ConnectRuralApiApplication.java  # Main entry point
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/
│   │           ├── db.changelog-master.xml
│   │           └── scripts/0.0.1.sql
│   └── test/
│       ├── java/com/crdev/connect_rural_api/
│       │   └── ConnectRuralApiApplicationTests.java
│       └── resources/
│           └── application-test.properties
├── .github/workflows/
│   ├── ci.yml                              # CI: build + test on push/PR
│   └── cd.yml                              # CD: separate build and test jobs
├── Dockerfile                              # Multi-stage Docker build
├── docker-compose.yml                      # Local dev orchestration
├── pom.xml                                 # Maven project descriptor
└── .env                                    # Local env vars (not committed to prod)
```

---

## Architectural Pattern

The codebase uses **Clean Architecture** with three clear layers:

```
app (REST) → business (use cases) → data (JPA repositories)
```

### Layer Responsibilities

| Layer | Package | Responsibility |
|---|---|---|
| `app` | `com.crdev.connect_rural_api.app` | HTTP controllers, request/response DTOs, global exception handling |
| `business` | `com.crdev.connect_rural_api.business` | Use cases, business rules, DTO-to-entity mapping, JPA Specifications |
| `data` | `com.crdev.connect_rural_api.data` | JPA entities, Spring Data repositories |

### Key Patterns Used

- **Use Case pattern**: Each operation has its own class (e.g., `CreateCommunityUseCase`, `GetResidentUseCase`). Never put business logic directly in controllers.
- **Repository pattern**: Data access goes through Spring Data JPA repositories only.
- **Mapper pattern**: DTO ↔ Entity conversion is done in dedicated `*AppMapper` classes in the `business` layer.
- **Specification pattern**: Dynamic JPA queries use `JpaSpecificationExecutor` and `Specification<T>` classes (see `resident/specs/`).

---

## API Endpoints

### Communities — `/api/communities`

| Method | Path | Description | Status |
|---|---|---|---|
| `POST` | `/api/communities` | Create community | 201 |
| `GET` | `/api/communities` | List all communities | 200 |
| `GET` | `/api/communities/{key}` | Get community by UUID key | 200 |
| `GET` | `/api/communities/paginated` | Paginated search (`keyword`, `page`, `size`) | 200 |
| `PATCH` | `/api/communities/{key}` | Partial update community | 200 |
| `DELETE` | `/api/communities/{key}` | Delete community | 204 |

### Residents — `/api/{communityKey}/residents`

| Method | Path | Description | Status |
|---|---|---|---|
| `POST` | `/api/{communityKey}/residents` | Create resident | 201 |
| `GET` | `/api/{communityKey}/residents` | List residents in a community | 200 |
| `GET` | `/api/{communityKey}/residents/paginated` | Paginated search (`keyword`, `page`, `size`) | 200 |
| `GET` | `/api/{communityKey}/residents/{residentKey}` | Get resident by UUID key | 200 |
| `PATCH` | `/api/{communityKey}/residents/{residentKey}` | Partial update resident | 200 |
| `DELETE` | `/api/{communityKey}/residents/{residentKey}` | Delete resident | 204 |

### Health

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Health check — returns plain text |

---

## Database Schema

### `communities`

| Column | Type | Notes |
|---|---|---|
| `community_key` | UUID (PK) | Auto-generated via `uuid_generate_v4()` |
| `name` | VARCHAR(200) | Required |
| `description` | TEXT | Optional |
| `logo_url` | VARCHAR(500) | Optional |
| `address` | VARCHAR(300) | Optional |
| `state` | VARCHAR(100) | Optional |
| `municipality` | VARCHAR(100) | Optional |
| `postal_code` | VARCHAR(20) | Optional |
| `subscription_plan` | VARCHAR(100) | Optional |
| `completed_configuration` | BOOLEAN | Default: false |
| `active` | BOOLEAN | Soft delete flag; default: true |
| `created_at` | TIMESTAMP | Auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

### `residents`

| Column | Type | Notes |
|---|---|---|
| `resident_key` | UUID (PK) | Auto-generated |
| `community_key` | UUID (FK) | References `communities(community_key)` ON DELETE CASCADE |
| `first_name` | VARCHAR(200) | Required |
| `last_name` | VARCHAR(200) | Optional |
| `birth_date` | DATE | Required |
| `phone_number` | VARCHAR(20) | Optional |
| `email` | VARCHAR(255) | Optional |
| `address` | TEXT | Optional |
| `address_reference` | TEXT | Optional |
| `joined_at` | TIMESTAMP | Default: current timestamp |
| `active` | BOOLEAN | Soft delete flag; default: true |
| `created_at` | TIMESTAMP | Auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

**Index:** `idx_resident_community_key` on `residents(community_key)`

---

## Common Development Commands

### Build

```bash
# Compile and run all checks (used in CI)
./mvnw clean verify

# Build JAR without running tests
./mvnw clean package -DskipTests
```

### Run Tests

```bash
# Run all tests with test profile (H2 in-memory, no migrations)
./mvnw test -Dspring.profiles.active=test
```

### Run Locally

```bash
# Start with Docker Compose (PostgreSQL + API)
docker-compose up

# Or run directly (requires a running PostgreSQL and .env variables set)
./mvnw spring-boot:run
```

### Environment Variables (`.env`)

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/connect_rural
DATABASE_USER=postgres
DATABASE_PASSWORD=<password>
```

The `application.properties` references these via `${DATABASE_URL}`, `${DATABASE_USER}`, `${DATABASE_PASSWORD}`.

---

## Configuration

### `application.properties` (production/local)

```properties
spring.application.name=connect-rural-api
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=none          # Schema managed by Liquibase only
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### `application-test.properties` (test profile)

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop   # Schema rebuilt per test run
spring.liquibase.enabled=false              # Migrations disabled for tests
```

### CORS

Configured in `CorsConfig.java`:
- Allowed origin: `http://localhost:4200` (Angular frontend)
- Allowed methods: GET, POST, PUT, DELETE, PATCH
- Credentials: enabled

---

## Coding Conventions

### Language

- **Code** (class names, method names, field names): English
- **Validation error messages**: Spanish (e.g., `"El nombre de la comunidad es obligatorio"`)
- **Git commit messages**: Spanish

### Lombok

All entities and DTOs use Lombok annotations. Common annotations:
- `@Data` — generates getters, setters, `equals`, `hashCode`, `toString`
- `@Builder` — builder pattern
- `@NoArgsConstructor` / `@AllArgsConstructor`
- `@Getter` / `@Setter`

Do not add manual getters/setters when Lombok is already in use.

### Validation

DTOs use Jakarta Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Size`, `@Email`, etc.). Error messages must be in Spanish. The `GlobalExceptionHandler` in `app/exceptions/` catches and formats validation errors automatically.

### Primary Keys

All entities use `UUID` as the primary key type, named `{entity}Key` (e.g., `communityKey`, `residentKey`). The database generates UUIDs via `uuid_generate_v4()`.

### Soft Deletes

Entities have an `active` boolean flag. "Deletion" sets `active = false` rather than removing the row. Queries should filter by `active = true` unless explicitly listing inactive records.

### Pagination

Paginated endpoints accept `keyword` (string search), `page` (0-indexed), and `size` query parameters. Use the existing `*FilterDto` and `*PaginatedResponseDto` patterns when adding new paginated endpoints.

---

## Adding New Features

When adding a new domain entity, follow this checklist:

1. **Data layer** (`data/<entity>/`):
   - Create `<Entity>Entity.java` with `@Entity`, `@Table`, Lombok, UUID PK
   - Create `<Entity>Repository.java` extending `JpaRepository<Entity, UUID>`
   - Add `JpaSpecificationExecutor<Entity>` if filtering is needed

2. **Database migration** (`resources/db/changelog/scripts/`):
   - Add a new `.sql` file with the `CREATE TABLE` statement
   - Register it in `db.changelog-master.xml`

3. **Business layer** (`business/<entity>/`):
   - Create one use case class per operation (Create, Get, Update, Delete, List, Paginated)
   - Create `<Entity>AppMapper.java` for DTO ↔ Entity conversion
   - Add specs in `specs/` if filtering is needed

4. **App layer** (`app/<entity>/`):
   - Create request DTOs (with validation) and response DTOs
   - Create `<Entity>Controller.java` with `@RestController`, `@RequestMapping`
   - Inject use cases via constructor injection

5. **Tests**: Add integration tests for the new controller/use cases.

---

## CI/CD Pipelines

### CI (`ci.yml`)

- **Triggers:** Push to any branch except `sandbox`/`production`; PRs targeting `sandbox` or `production`
- **Steps:** Checkout → Java 21 setup → Maven cache → `mvn clean verify`

### CD (`cd.yml`)

- **Triggers:** Push/PR to any branch except `sandbox`/`production`
- **Jobs (parallel):**
  - `build`: `mvn clean package -DskipTests`
  - `tests` (depends on build): `mvn test -Dspring.profiles.active=test`

---

## Docker

### Dockerfile (multi-stage)

1. **Build stage**: Uses Maven image to run `mvn clean package -DskipTests`
2. **Runtime stage**: Uses Java 21 slim image; copies the built JAR

### Docker Compose

- Spins up the API service on port `8080`
- Sets `SPRING_PROFILES_ACTIVE=docker`
- Expects a running PostgreSQL instance (configure in `docker-compose.yml` or externally)

---

## Important Notes for AI Assistants

- **Never modify schema via JPA DDL.** Schema changes must go through Liquibase migration scripts.
- **Never skip the `app → business → data` separation.** Controllers must delegate to use cases, not repositories directly.
- **Test profile uses H2** — tests do not require a running PostgreSQL. Do not add PostgreSQL-specific SQL to test configurations.
- **Add one use case per operation.** Resist the urge to combine logic into a generic service class.
- **Do not hardcode credentials.** All secrets go in environment variables referenced by `application.properties`.
- **Validation messages in Spanish.** Follow the existing convention for all new DTOs.
- **UUID keys, not sequential IDs.** All new entities must use UUID primary keys.
- **Soft deletes via `active` flag.** Do not physically delete records unless explicitly required.
