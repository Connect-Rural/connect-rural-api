---
name: connect-rural-usecase
description: "Guide for adding a new use case to an existing module in connect-rural-api. Use when a module already exists but needs a new operation: a custom query, an action that crosses multiple services, a specialized detail endpoint, or any logic beyond basic CRUD. Examples: 'add an endpoint that returns payment stats', 'add a use case that aggregates data from two services', 'add a bulk action use case'."
---

# Connect Rural Use Case

## Overview

Use cases are `@Component @AllArgsConstructor` classes with a single `execute(...)` method. Each use case lives in `business/{module}/usecases/`. After creating the use case, wire it into the existing controller.

See `references/templates.md` for code templates.

## Workflow

### Step 1: Classify the use case

**Simple** — reads or writes using a single service, no cross-service coordination:
- `GetXByKeyUseCase`, `DeleteXUseCase`, basic `CreateXUseCase`
- No `@Transactional` needed

**Aggregate** — reads from multiple services to build an enriched response:
- Example: `GetCooperationDetailByKeyUseCase` (cooperation + cooperationResident + resident)
- Add `@Transactional` to ensure consistent reads across services

**Transactional write** — creates/updates entities across multiple services atomically:
- Example: `CreateCooperationUseCase` (creates cooperation then assigns residents)
- Add `@Transactional` so a failure mid-operation rolls everything back

### Step 2: Create the use case class

File: `business/{module}/usecases/{Action}{Module}UseCase.java`

- Annotate with `@Component @AllArgsConstructor`
- Inject only what is needed: Services + Mapper
- Add `@Transactional` for aggregations and multi-service writes
- Keep the method signature `execute(String communityKey, ...)` for community-scoped modules

See `references/templates.md` for templates by type.

### Step 3: Add new response DTO if needed

If the use case returns a shape that doesn't match existing DTOs, create a new response DTO in `app/{module}/dto/response/`. Use `@Data @AllArgsConstructor @NoArgsConstructor`.

### Step 4: Add method to Mapper if needed

If the new response DTO needs a mapping method, add it to `business/{module}/mapper/{Module}AppMapper.java`:
- Method name: `to{ResponseDto}(entity, ...extraParams)`
- Return `null` if input entity is `null`

### Step 5: Wire into Controller

In `app/{module}/{Module}Controller.java`:

1. Add the new use case as a `private final` field (Lombok injects via `@RequiredArgsConstructor`)
2. Add the endpoint method following the existing patterns:
   - New detail/sub-resource: `@GetMapping("/{key}/detail")` or `@GetMapping("/{key}/{sub}")`
   - New action: `@PostMapping("/{key}/{action}")`

## Key Rules

- **One class per operation** — never add a second `execute()` or share logic between use cases
- **No repository access in use cases** — always go through a Service
- **No mapping in use cases** — always delegate to the Mapper
- **`@Transactional` on the use case, not the service** — services stay non-transactional by default