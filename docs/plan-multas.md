# Plan: Módulo de Multas

## Contexto

El modelo de datos ya está esbozado en `0.0.5.sql` (comentado). Las multas son cargos por incumplimiento de reglamento, asistencia u otras obligaciones comunitarias. Se integran con `financial_obligations` via `origin_type = 'FINE'`, por lo que el historial de pagos y el flujo de cobro es el mismo que el de cooperaciones.

---

## Modelo de datos

### `fines` — catálogo de tipos de multa por comunidad

```sql
CREATE TABLE connect_rural.fines (
    fine_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     TEXT NULL,
    default_amount  DECIMAL(12,2) NOT NULL,
    reason_type     VARCHAR(100) NULL,   -- INASISTENCIA | INCUMPLIMIENTO | DAÑO | OTRO
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fine_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_fine_community ON connect_rural.fines(community_key);
```

### `fine_assignments` — multas aplicadas a residentes

```sql
CREATE TABLE connect_rural.fine_assignments (
    assignment_key  UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    fine_key        UUID NOT NULL,
    resident_key    UUID NOT NULL,
    community_key   UUID NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,   -- puede diferir del default_amount
    reason          TEXT NULL,                -- descripción específica del caso
    issued_at       DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fassign_fine      FOREIGN KEY (fine_key)
        REFERENCES connect_rural.fines(fine_key) ON DELETE RESTRICT,
    CONSTRAINT fk_fassign_resident  FOREIGN KEY (resident_key)
        REFERENCES connect_rural.residents(resident_key) ON DELETE CASCADE,
    CONSTRAINT fk_fassign_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE
);
```

Al crear un `fine_assignment` se genera automáticamente una `financial_obligation` con `origin_type='FINE'` y `origin_id=assignment_key`.

---

## Estructura de código

```
app/fine/
  ├── FineController.java
  └── dto/
      ├── CreateFineRequest.java
      ├── AssignFineRequest.java
      ├── FineResponse.java
      └── FineAssignmentResponse.java

business/fine/
  ├── FineService.java              ← create, update, delete, list, assignToResident, getResidentFines
  ├── FineRepository.java           ← interfaz (puerto BD fines)
  ├── FineAssignmentRepository.java ← interfaz (puerto BD fine_assignments)
  └── FineMapper.java

data/fine/
  ├── FineRepositoryImpl.java
  ├── FineJpaRepository.java
  ├── FineEntity.java
  ├── FineAssignmentRepositoryImpl.java
  ├── FineAssignmentJpaRepository.java
  └── FineAssignmentEntity.java
```

---

## Endpoints

```
# Catálogo de multas
GET    /api/{communityKey}/fines
POST   /api/{communityKey}/fines
PUT    /api/{communityKey}/fines/{fineKey}
DELETE /api/{communityKey}/fines/{fineKey}

# Aplicar multa a residente
POST   /api/{communityKey}/fines/{fineKey}/assign
  Body: { residentKey, amount?, reason, issuedAt? }

# Ver multas de un residente
GET    /api/{communityKey}/residents/{residentKey}/fines

# Pagar multa (reutiliza el mismo flujo de cooperaciones)
PATCH  /api/{communityKey}/fines/assignments/{assignmentKey}/pay
PATCH  /api/{communityKey}/fines/assignments/{assignmentKey}/unpay
```

---

## Integración con `financial_obligations`

`FineService.assignToResident()` al crear la asignación:

```java
// FineService inyecta FinancialObligationRepository directamente
financialObligationRepository.save(new FinancialObligationEntity(
    assignmentKey,   // origin_id
    "FINE",          // origin_type
    communityKey,
    residentKey,
    amount,
    issuedAt         // due_date = issuedAt + community grace period
));
```

`FineService` inyecta `FinancialObligationRepository` (la interfaz) — no llama a `FinancialObligationService`.

Los endpoints de pay/unpay pueden reutilizar la lógica existente en `CooperationService` a través de `ResidentPaymentRepository` y `FinancialObligationRepository` sin pasar por otro Service.

---

## Orden de implementación

- [ ] 1. Migración: tablas `fines` y `fine_assignments`
- [ ] 2. Entidades + JpaRepositories + Repository interfaces + RepositoryImpls
- [ ] 3. DTOs request/response
- [ ] 4. `FineMapper`
- [ ] 5. CRUD del catálogo — métodos en `FineService`
- [ ] 6. `FineService.assignToResident()` — crea asignación + financial_obligation
- [ ] 7. `FineService.getResidentFines()`
- [ ] 8. `FineController`
- [ ] 9. Endpoints de pay/unpay para multas
