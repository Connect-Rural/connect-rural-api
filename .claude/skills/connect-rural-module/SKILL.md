---
name: connect-rural-module
description: "Guide for creating new domain modules in the connect-rural-api Spring Boot project. Use when adding a new entity, CRUD feature, or domain (e.g., 'add payments module', 'create invoices entity', 'implement CRUD for categories'). Covers the 3-layer architecture: data (Entity + Repository), business (Service + UseCases + Mapper + Specs), and app (Controller + DTOs)."
---

# Connect Rural Module Creator

## Overview

Every new domain module spans three packages following the project's layered architecture:
- `data/{module}` - Entity + Repository
- `business/{module}` - Service + UseCases + Mapper + (Specs if filtered)
- `app/{module}` - Controller + dto/request/ + dto/response/

Read `references/templates.md` for complete code templates for each file.

## Workflow

### Step 1: Determine scope

**Top-level module** (e.g., community): Routes at `/api/{modules}`, no `communityKey` scoping.

**Community-scoped module** (e.g., resident, cooperation): Routes at `/api/{communityKey}/{modules}`, all service queries filtered by `communityKey`.

### Step 2: Create Data layer

1. **Entity** (`data/{module}/{Module}Entity.java`):
   - `@Entity @Table(name = "{table_name}")`
   - `@Data @NoArgsConstructor @AllArgsConstructor` (Lombok)
   - UUID PK with `@GeneratedValue(strategy = GenerationType.UUID)`, column named `{module}_key`
   - Timestamps: `created_at` (`updatable = false`) and `updated_at`

2. **Repository** (`data/{module}/{Module}Repository.java`):
   - Extends `JpaRepository<{Module}Entity, UUID>` + `JpaSpecificationExecutor<{Module}Entity>`

3. **Migration SQL** (`resources/db/changelog/scripts/{version}.sql`):
   - Add the new changeset to `db.changelog-master.xml`

### Step 3: Create Business layer

3. **Service** (`business/{module}/{Module}Service.java`):
   - `@Service @RequiredArgsConstructor`
   - Methods: `getAll()`, `getAllPaginatedAndFiltered(...)`, `getByKey(String key)`, `create(entity)`, `update(entity)`, `delete(String key)`
   - `getByKey` throws `IllegalArgumentException("Entity not found")` if absent

4. **Use Cases** (`business/{module}/usecases/`): One `@Component @AllArgsConstructor` class per operation, each with a single `execute(...)` method. Standard set:
   - `Create{Module}UseCase` - maps DTO to entity, calls service.create, returns response DTO
   - `Get{Module}ListUseCase` - calls service.getAll, maps list
   - `Get{Module}PaginatedUseCase` - accepts filter DTO, calls paginated service, builds paginated response
   - `Get{Module}ByKeyUseCase` - calls service.getByKey, maps to response
   - `Update{Module}UseCase` - loads entity, calls mapper.updateFromDto, calls service.update
   - `Delete{Module}UseCase` - calls service.delete (no return)

5. **Mapper** (`business/{module}/mapper/{Module}AppMapper.java`):
   - `@Component` (no constructor injection - uses plain `@Component`)
   - `toResponse(entity)`, `toAdminResponse(entity)` if needed, `updateFromDto(dto, entity)`, list mapping helpers

6. **Specs** (`business/{module}/specs/{Module}Specs.java`) - only if filtered queries needed:
   - Static factory methods returning `Specification<{Module}Entity>`
   - Use `withCommunity(UUID communityKey)` for community scoping + `withKeyword(String keyword)` for search

### Step 4: Create App layer

7. **Request DTOs** (`app/{module}/dto/request/`):
   - `Create{Module}Dto` - `@Data @AllArgsConstructor @NoArgsConstructor`, validation annotations on fields
   - `{Module}FilterDto` - fields: `keyword`, `page`, `size`

8. **Response DTOs** (`app/{module}/dto/response/`):
   - `{Module}ResponseDto` - flat response for create/get
   - `{Module}PaginatedResponseDto` - wraps `List<{Module}ResponseDto>` + `page`, `size`, `totalElements`, `totalPages`

9. **Controller** (`app/{module}/{Module}Controller.java`):
   - `@RestController @RequestMapping("/api/...") @RequiredArgsConstructor`
   - Inject all use cases (no services, no mappers directly)
   - POST returns `ResponseEntity.status(201).body(...)`
   - DELETE returns `ResponseEntity.noContent().build()`
   - Paginated: accepts `@RequestParam` with defaults, builds FilterDto, delegates to use case

## Key Conventions

- All primary keys are `UUID`, exposed as `String` in DTOs (`key.toString()`)
- `active` boolean for soft deletes where applicable
- Use `@Valid` on `@RequestBody` params; validation errors handled globally
- Mappers never throw; return `null` if input is `null`
- Use `@AllArgsConstructor` (not `@RequiredArgsConstructor`) on use cases since all fields are final

See `references/templates.md` for copy-paste code templates for all files.