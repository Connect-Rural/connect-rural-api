# Plan: Gestión de Asambleas

## Contexto

Las comunidades rurales toman decisiones colectivas en asambleas. Digitalizar el registro de reuniones, asistencia, acuerdos y votaciones tiene valor legal y organizacional — reemplaza libretas físicas y da trazabilidad histórica.

---

## Conceptos del módulo

| Concepto | Descripción |
|----------|-------------|
| `assembly` | Reunión comunitaria con fecha, lugar y tipo |
| `attendance` | Registro de asistencia por residente |
| `agreement` | Acuerdo tomado en la asamblea |
| `vote` | Votación sobre un acuerdo específico |

---

## Modelo de datos

### `assemblies` — registro de reuniones

```sql
CREATE TABLE connect_rural.assemblies (
    assembly_key    UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    assembly_type   VARCHAR(50) NOT NULL DEFAULT 'ORDINARY',   -- ORDINARY | EXTRAORDINARY
    status          VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED | IN_PROGRESS | CLOSED
    scheduled_at    TIMESTAMP NOT NULL,
    location        VARCHAR(255) NULL,
    agenda          TEXT NULL,
    minutes         TEXT NULL,          -- acta final redactada
    quorum_required INT NULL,           -- % mínimo de asistencia para validez
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assembly_type   CHECK (assembly_type IN ('ORDINARY','EXTRAORDINARY')),
    CONSTRAINT chk_assembly_status CHECK (status IN ('SCHEDULED','IN_PROGRESS','CLOSED')),
    CONSTRAINT fk_assembly_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_assembly_community ON connect_rural.assemblies(community_key);
```

### `assembly_attendance` — asistencia

```sql
CREATE TABLE connect_rural.assembly_attendance (
    attendance_key  UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    assembly_key    UUID NOT NULL,
    resident_key    UUID NOT NULL,
    attended        BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attendance UNIQUE (assembly_key, resident_key),
    CONSTRAINT fk_attend_assembly FOREIGN KEY (assembly_key)
        REFERENCES connect_rural.assemblies(assembly_key) ON DELETE CASCADE,
    CONSTRAINT fk_attend_resident FOREIGN KEY (resident_key)
        REFERENCES connect_rural.residents(resident_key) ON DELETE CASCADE
);
```

### `assembly_agreements` — acuerdos

```sql
CREATE TABLE connect_rural.assembly_agreements (
    agreement_key   UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    assembly_key    UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',    -- PENDING | APPROVED | REJECTED
    votes_for       INT NOT NULL DEFAULT 0,
    votes_against   INT NOT NULL DEFAULT 0,
    votes_abstain   INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_agreement_status CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    CONSTRAINT fk_agreement_assembly FOREIGN KEY (assembly_key)
        REFERENCES connect_rural.assemblies(assembly_key) ON DELETE CASCADE
);
```

### `assembly_votes` — votos individuales por residente

```sql
CREATE TABLE connect_rural.assembly_votes (
    vote_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    agreement_key   UUID NOT NULL,
    resident_key    UUID NOT NULL,
    vote            VARCHAR(20) NOT NULL,   -- FOR | AGAINST | ABSTAIN
    voted_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_vote UNIQUE (agreement_key, resident_key),
    CONSTRAINT chk_vote CHECK (vote IN ('FOR','AGAINST','ABSTAIN')),
    CONSTRAINT fk_vote_agreement FOREIGN KEY (agreement_key)
        REFERENCES connect_rural.assembly_agreements(agreement_key) ON DELETE CASCADE,
    CONSTRAINT fk_vote_resident  FOREIGN KEY (resident_key)
        REFERENCES connect_rural.residents(resident_key) ON DELETE CASCADE
);
```

---

## Flujo principal

```
1. Admin crea asamblea con agenda y fecha → status=SCHEDULED
2. El día de la asamblea: admin la abre → status=IN_PROGRESS
3. Admin registra asistencia (lista de residentes presentes)
4. Por cada punto del orden del día: crea acuerdo + registra votos
5. Admin cierra la asamblea → status=CLOSED
6. Admin redacta y guarda el acta (campo `minutes`)
7. (Opcional) Exportar acta en PDF
```

---

## Endpoints

```
# Asambleas
GET    /api/{communityKey}/assemblies
POST   /api/{communityKey}/assemblies
GET    /api/{communityKey}/assemblies/{assemblyKey}
PATCH  /api/{communityKey}/assemblies/{assemblyKey}/open
PATCH  /api/{communityKey}/assemblies/{assemblyKey}/close
PUT    /api/{communityKey}/assemblies/{assemblyKey}/minutes   ← guardar acta

# Asistencia
POST   /api/{communityKey}/assemblies/{assemblyKey}/attendance
  Body: { residentKeys: [...] }                               ← registro masivo

GET    /api/{communityKey}/assemblies/{assemblyKey}/attendance

# Acuerdos
GET    /api/{communityKey}/assemblies/{assemblyKey}/agreements
POST   /api/{communityKey}/assemblies/{assemblyKey}/agreements
PATCH  /api/{communityKey}/assemblies/{assemblyKey}/agreements/{agreementKey}/vote
  Body: { residentKey, vote: "FOR|AGAINST|ABSTAIN" }
```

---

## Estructura de código

```
app/assembly/
  ├── AssemblyController.java
  └── dto/
      ├── CreateAssemblyRequest.java
      ├── RegisterAttendanceRequest.java
      ├── CreateAgreementRequest.java
      ├── RegisterVoteRequest.java
      ├── AssemblyResponse.java
      └── AssemblyDetailResponse.java

business/assembly/
  ├── AssemblyService.java               ← create, open, close, saveMinutes, registerAttendance,
  │                                         createAgreement, registerVote
  ├── AssemblyRepository.java            ← interfaz (puerto BD assemblies)
  ├── AssemblyAttendanceRepository.java  ← interfaz (puerto BD)
  ├── AssemblyAgreementRepository.java   ← interfaz (puerto BD)
  ├── AssemblyVoteRepository.java        ← interfaz (puerto BD)
  └── AssemblyMapper.java

data/assembly/
  ├── AssemblyRepositoryImpl.java + JpaRepository + Entity
  ├── AssemblyAttendanceRepositoryImpl.java + JpaRepository + Entity
  ├── AssemblyAgreementRepositoryImpl.java + JpaRepository + Entity
  └── AssemblyVoteRepositoryImpl.java + JpaRepository + Entity
```

`AssemblyService` inyecta `ResidentRepository` para validar que los residentes existen en la comunidad — no llama a `ResidentService`.

---

## Orden de implementación

- [ ] 1. Migración: 4 tablas del módulo
- [ ] 2. Entidades + JpaRepositories + Repository interfaces + RepositoryImpls
- [ ] 3. DTOs + `AssemblyMapper`
- [ ] 4. CRUD asambleas + `open()` / `close()` / `saveMinutes()`
- [ ] 5. `AssemblyService.registerAttendance()` — registro masivo
- [ ] 6. `AssemblyService.createAgreement()` + `registerVote()`
- [ ] 7. `AssemblyController` + endpoints
- [ ] 8. (Opcional) Exportar acta PDF — integrar con `plan-reportes-exportacion.md`

---

## Consideraciones

- El quórum se valida al cerrar la asamblea: `asistentes / total_residentes_activos >= quorum_required%`
- `assembly_votes` registra el voto individual para trazabilidad — `votes_for/against/abstain` en `agreements` son contadores denormalizados para consulta rápida
- Una vez `CLOSED`, la asamblea es de solo lectura
- El acta (`minutes`) debería poder exportarse como PDF junto con la lista de asistencia y acuerdos
