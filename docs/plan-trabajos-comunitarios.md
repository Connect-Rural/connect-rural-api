# Plan: Trabajos Comunitarios

## Contexto

El modelo está esbozado en `0.0.5.sql` (comentado): `community_works`, `work_assignments` y `work_settlements`. Permite asignar faenas o trabajos a residentes y opcionalmente saldar deudas (`financial_obligations`) con trabajo realizado.

---

## Modelo de datos

### `community_works` — catálogo de trabajos por comunidad

```sql
CREATE TABLE connect_rural.community_works (
    work_key            UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key       UUID NOT NULL,
    name                VARCHAR(150) NOT NULL,
    description         TEXT NULL,
    equivalent_amount   DECIMAL(12,2) NOT NULL,   -- valor económico del trabajo
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_work_community ON connect_rural.community_works(community_key);
```

### `work_assignments` — asignación de trabajo a residente

```sql
CREATE TABLE connect_rural.work_assignments (
    assignment_key  UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    resident_key    UUID NOT NULL,
    community_key   UUID NOT NULL,
    work_key        UUID NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    scheduled_date  DATE NULL,
    completed_at    TIMESTAMP NULL,
    approved_by     UUID NULL,
    notes           TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wassign_status CHECK (status IN ('PENDING','COMPLETED','CANCELLED')),
    CONSTRAINT fk_wassign_resident  FOREIGN KEY (resident_key)
        REFERENCES connect_rural.residents(resident_key) ON DELETE CASCADE,
    CONSTRAINT fk_wassign_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT fk_wassign_work      FOREIGN KEY (work_key)
        REFERENCES connect_rural.community_works(work_key) ON DELETE RESTRICT
);
```

### `work_settlements` — trabajo aplicado como abono a una deuda

```sql
CREATE TABLE connect_rural.work_settlements (
    settlement_key      UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    obligation_key      UUID NOT NULL,      -- financial_obligation que se salda
    assignment_key      UUID NOT NULL,      -- trabajo que respalda el abono
    amount_equivalent   DECIMAL(12,2) NOT NULL,
    approved_by         UUID NULL,
    notes               TEXT NULL,
    settled_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wsettle_obligation FOREIGN KEY (obligation_key)
        REFERENCES connect_rural.financial_obligations(obligation_key) ON DELETE RESTRICT,
    CONSTRAINT fk_wsettle_assignment FOREIGN KEY (assignment_key)
        REFERENCES connect_rural.work_assignments(assignment_key) ON DELETE RESTRICT
);
```

---

## Flujo principal

```
1. Admin crea catálogo de trabajos (limpieza, reparación, guardia, etc.)
2. Admin asigna trabajo a residente → status=PENDING
3. Residente completa el trabajo
4. Admin marca como COMPLETED
5. (Opcional) Admin aplica el trabajo como abono a una deuda del residente
   → crea work_settlement + marca financial_obligation como PAID
```

---

## Estructura de código

```
app/work/
  ├── CommunityWorkController.java
  └── dto/
      ├── CreateCommunityWorkRequest.java
      ├── AssignWorkRequest.java
      ├── SettleWorkRequest.java
      └── WorkAssignmentResponse.java

business/work/
  ├── CommunityWorkService.java           ← CRUD catálogo + assign + complete + cancel + settle
  ├── CommunityWorkRepository.java        ← interfaz (puerto BD community_works)
  ├── WorkAssignmentRepository.java       ← interfaz (puerto BD work_assignments)
  ├── WorkSettlementRepository.java       ← interfaz (puerto BD work_settlements)
  └── WorkMapper.java

data/work/
  ├── CommunityWorkRepositoryImpl.java
  ├── CommunityWorkJpaRepository.java
  ├── CommunityWorkEntity.java
  ├── WorkAssignmentRepositoryImpl.java
  ├── WorkAssignmentJpaRepository.java
  ├── WorkAssignmentEntity.java
  ├── WorkSettlementRepositoryImpl.java
  ├── WorkSettlementJpaRepository.java
  └── WorkSettlementEntity.java
```

`CommunityWorkService.settleAsPayment()` inyecta `FinancialObligationRepository` directamente para marcar la obligation — no llama a otros Services.

---

## Endpoints

```
# Catálogo
GET    /api/{communityKey}/works
POST   /api/{communityKey}/works
PUT    /api/{communityKey}/works/{workKey}
DELETE /api/{communityKey}/works/{workKey}

# Asignaciones
POST   /api/{communityKey}/works/{workKey}/assign
  Body: { residentKey, scheduledDate?, notes? }

PATCH  /api/{communityKey}/works/assignments/{assignmentKey}/complete
PATCH  /api/{communityKey}/works/assignments/{assignmentKey}/cancel

GET    /api/{communityKey}/residents/{residentKey}/works

# Liquidación como pago
POST   /api/{communityKey}/works/assignments/{assignmentKey}/settle
  Body: { obligationKey, notes? }
```

---

## Orden de implementación

- [ ] 1. Migración: tablas `community_works`, `work_assignments`, `work_settlements`
- [ ] 2. Entidades + JpaRepositories + Repository interfaces + RepositoryImpls
- [ ] 3. DTOs request/response + `WorkMapper`
- [ ] 4. CRUD catálogo de trabajos — métodos en `CommunityWorkService`
- [ ] 5. `CommunityWorkService.assign()`
- [ ] 6. `CommunityWorkService.complete()` + `cancel()`
- [ ] 7. `CommunityWorkService.settleAsPayment()` — lógica de abono a deuda
- [ ] 8. `CommunityWorkService.getResidentWorkHistory()`
- [ ] 9. `CommunityWorkController` + endpoints

---

## Consideraciones

- `settleAsPayment()` debe validar que el `work_assignment` esté en estado `COMPLETED` antes de aplicarlo como pago
- Un `work_assignment` solo puede usarse para saldar una obligation (relación 1:1 en `work_settlements`)
- El `equivalent_amount` del trabajo puede no cubrir el total de la deuda — en ese caso la obligation queda parcialmente pagada o se registra un pago parcial
