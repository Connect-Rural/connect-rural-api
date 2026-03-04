---
name: connect-rural-migration
description: "Guide for writing Liquibase SQL migrations in connect-rural-api. Use when creating a new table, adding columns, modifying a column type, adding indexes, or adding foreign keys. Covers the project's changeset ID format, UUID strategy, FK naming, index naming, and master.xml setup."
---

# Connect Rural Migration

## Overview

Migrations are plain SQL files with Liquibase annotations placed in `src/main/resources/db/changelog/scripts/`. The master changelog uses `<includeAll>` so **new files are picked up automatically by alphabetical order — no need to edit `db.changelog-master.xml`**.

See `references/patterns.md` for SQL templates and naming conventions.

## Workflow

### Step 1: Choose filename

Files execute in alphabetical order. Use the format:

```
{version}.sql          # e.g., 0.0.2.sql, 1.0.0.sql
```

Or append a descriptive suffix when adding to an existing version:

```
0.0.2-add-payments.sql
```

### Step 2: Write the file header

Every migration file starts with:

```sql
-- liquibase formatted sql
```

### Step 3: Write changesets

Each logical change is its own changeset:

```sql
-- changeset {author}:{DDMMYY}-{sequence}
```

- **Author**: use `israel-CR` (consistent with existing changesets)
- **Date**: `DDMMYY` format (e.g., `240226` for Feb 24, 2026)
- **Sequence**: 3-digit counter starting at `001` per date

Example: `-- changeset israel-CR:240226-001`

One changeset per table creation. Group related `CREATE INDEX` statements in the same changeset as the table they index.

### Step 4: Apply conventions

See `references/patterns.md` for:
- UUID primary key setup (`uuid_generate_v4()`)
- FK constraint naming: `fk_{child_table}_{parent_table}`
- Index naming: `idx_{table}_{column}`
- Standard column types and defaults

### Step 5: Verify

Run the app (`mvn spring-boot:run`) and confirm Liquibase applies the changeset without errors. The test profile disables Liquibase, so verify against the local PostgreSQL instance.