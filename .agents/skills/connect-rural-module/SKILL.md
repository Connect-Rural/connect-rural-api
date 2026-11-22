---
name: connect-rural-module
description: "Guide for adding or migrating a domain in the current Maven multimodule Connect Rural API. Use when adding an entity, CRUD feature, or moving domain logic between api, services, repositories, and models."
---

# Connect Rural domain module workflow

## Source of truth

Read `docs/architecture.md` and inspect the current code before making changes. The migration is in progress: communities and residents work, but not every domain follows the same internal structure. Do not treat old templates or the existence of `api/app`, `api/business`, and `api/data` as proof that every domain has migrated.

## Current Maven responsibilities

- `models`: shared JPA entities.
- `repositories`: Spring Data repositories and persistence queries/specifications.
- `services`: domain services and domain/query models.
- `api`: Spring Boot entry point, HTTP controllers/DTOs, configuration, and Liquibase migrations.

Verify module POMs before adding dependencies. Keep dependency direction consistent with the current build: `repositories -> models`, `services -> repositories + models`, `api -> services + models`. Avoid introducing circular Maven dependencies.

## Workflow

1. **Trace an existing working domain.** For a community-level endpoint, inspect `api/app/community`, `services/service/community`, `repositories/repository/community`, and `models/model/CommunityEntity.java`. For community-scoped endpoints, inspect the corresponding resident files.
2. **Determine whether this is a new domain or a migration.** For a migration, identify the active call path and existing tests first. Do not create a parallel service/repository implementation.
3. **Keep HTTP concerns in `api`.** Controllers handle HTTP status, path/query/body binding, validation, and request/response mapping. Keep API DTOs in the API module.
4. **Keep domain operations in `services`.** Use domain models/commands/queries rather than coupling service contracts to HTTP DTOs. Follow the concrete conventions of the closest migrated domain.
5. **Keep persistence access in `repositories` and entities in `models`.** Add or change repository methods only when required by the domain operation. Avoid exposing persistence details unnecessarily through service contracts.
6. **Scope community data correctly.** For community-scoped resources, ensure reads, updates, and deletes validate both the resource key and the owning `communityKey`.
7. **Use Liquibase for schema changes.** Follow `.agents/skills/connect-rural-migration/SKILL.md`; confirm the master changelog's actual inclusion behavior rather than assuming it.
8. **Preserve contracts.** Do not change endpoint paths, response shapes, soft-delete semantics, or database constraints unintentionally.
9. **Test each layer appropriately.** Add service tests for business rules, repository/integration tests for persistence behavior, and API tests for HTTP validation/contracts where relevant. Run `mvn clean verify` when feasible.
10. **Update architecture documentation** when the active path or migration status changes.

## Naming and mapping

Follow the naming conventions used by the closest working domain rather than copying legacy templates blindly. Keep API DTOs distinct from service/domain models when practical. Mapping between API DTOs, domain models, and JPA entities should be explicit and located in the layer that owns each boundary.

## Do not

- Do not assume a hexagonal/clean architecture is fully implemented.
- Do not put Spring Data/JPA types into service APIs without first checking the established pattern and migration decision.
- Do not make controllers call repositories directly.
- Do not call another domain's service directly just to bypass a missing domain operation; introduce an explicit contract or coordinate the design.
- Do not add authentication based on obsolete role plans. CU-CR-022 depends on the definition of CU-CR-023 (users/roles) and CU-CR-025 (security audit).
- Do not claim tests/build pass unless they were actually run.
