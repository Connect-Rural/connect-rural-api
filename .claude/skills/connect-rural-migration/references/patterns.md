# Migration SQL Patterns

## File structure

```sql
-- liquibase formatted sql

-- changeset israel-CR:240226-001
-- (DDL here)

-- changeset israel-CR:240226-002
-- (next change here)
```

---

## New table (community-scoped)

```sql
-- changeset israel-CR:{DDMMYY}-001
CREATE TABLE IF NOT EXISTS {table_name} (
    {table_singular}_key  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    community_key         UUID NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    description           TEXT NULL,
    active                BOOLEAN DEFAULT TRUE,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_{table_name}_communities FOREIGN KEY (community_key)
        REFERENCES communities(community_key) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_{table_name}_community_key ON {table_name}(community_key);
```

## New table (top-level, no community FK)

```sql
-- changeset israel-CR:{DDMMYY}-001
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;  -- only if not already present

CREATE TABLE IF NOT EXISTS {table_name} (
    {table_singular}_key  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                  VARCHAR(200) NOT NULL,
    active                BOOLEAN DEFAULT TRUE,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Junction / association table

```sql
-- changeset israel-CR:{DDMMYY}-001
CREATE TABLE IF NOT EXISTS {table_a}_{table_b} (
    {table_a}_{table_b}_key  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    {table_a_singular}_key   UUID NOT NULL,
    {table_b_singular}_key   UUID NOT NULL,
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_{table_a}_{table_b}_{table_a} FOREIGN KEY ({table_a_singular}_key)
        REFERENCES {table_a}({table_a_singular}_key) ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_{table_a}_{table_b}_{table_b} FOREIGN KEY ({table_b_singular}_key)
        REFERENCES {table_b}({table_b_singular}_key) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_{table_a}_{table_b}_{table_a_singular}_key ON {table_a}_{table_b}({table_a_singular}_key);
CREATE INDEX IF NOT EXISTS idx_{table_a}_{table_b}_{table_b_singular}_key ON {table_a}_{table_b}({table_b_singular}_key);
```

## Add column to existing table

```sql
-- changeset israel-CR:{DDMMYY}-001
ALTER TABLE {table_name}
    ADD COLUMN IF NOT EXISTS {column_name} {TYPE} {NULL|NOT NULL} {DEFAULT ...};
```

## Add index to existing table

```sql
-- changeset israel-CR:{DDMMYY}-001
CREATE INDEX IF NOT EXISTS idx_{table_name}_{column_name} ON {table_name}({column_name});
```

---

## Column type reference

| Use case | SQL type |
|---|---|
| Short text (name, status) | `VARCHAR(200)` |
| Long text (description, notes) | `TEXT` |
| Money / amounts | `DECIMAL(12,2)` |
| Date only | `DATE` |
| Date + time | `TIMESTAMP` |
| Flag | `BOOLEAN DEFAULT TRUE` or `DEFAULT FALSE` |
| UUID FK | `UUID NOT NULL` |
| Optional UUID FK | `UUID NULL` |

## Naming conventions

| Object | Pattern | Example |
|---|---|---|
| PK column | `{singular}_key` | `cooperation_key` |
| FK column | `{referenced_singular}_key` | `community_key` |
| FK constraint | `fk_{child}_{parent}` | `fk_residents_communities` |
| Index | `idx_{table}_{column}` | `idx_residents_community_key` |