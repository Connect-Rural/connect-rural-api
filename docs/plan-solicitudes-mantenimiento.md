# Plan: Solicitudes de Mantenimiento

## Contexto

Los residentes reportan problemas en la comunidad (calles, tuberías, infraestructura) y el admin los gestiona hasta su resolución. Conecta con el módulo de trabajos comunitarios — un problema puede resolverse asignando un trabajo comunitario. También conecta con el módulo contable — una reparación puede generar un egreso.

---

## Flujo principal

```
1. Residente reporta un problema (desde portal o el admin lo reporta)
2. Admin revisa → cambia status a IN_PROGRESS, asigna responsable
3. (Opcional) Crea un work_assignment para resolverlo con trabajo comunitario
4. (Opcional) Registra un expense por la reparación
5. Admin cierra la solicitud → status=RESOLVED
```

---

## Modelo de datos

### `maintenance_categories` — tipos de problema

```sql
CREATE TABLE connect_rural.maintenance_categories (
    category_key    UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    name            VARCHAR(100) NOT NULL,   -- Vialidad, Agua, Electricidad, Seguridad, Otro
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_maintcat_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE
);
```

### `maintenance_requests` — solicitudes

```sql
CREATE TABLE connect_rural.maintenance_requests (
    request_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    resident_key    UUID NULL,              -- NULL si lo reporta el admin directamente
    category_key    UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NULL,
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',  -- LOW | MEDIUM | HIGH | URGENT
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_to     UUID NULL,              -- user_key del responsable
    resolved_at     TIMESTAMP NULL,
    resolution_note TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_maint_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    CONSTRAINT chk_maint_status   CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CANCELLED')),
    CONSTRAINT fk_maint_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT fk_maint_resident  FOREIGN KEY (resident_key)
        REFERENCES connect_rural.residents(resident_key) ON DELETE SET NULL,
    CONSTRAINT fk_maint_category  FOREIGN KEY (category_key)
        REFERENCES connect_rural.maintenance_categories(category_key) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_maint_community_status ON connect_rural.maintenance_requests(community_key, status);
```

### `maintenance_attachments` — fotos o documentos del problema

```sql
CREATE TABLE connect_rural.maintenance_attachments (
    attachment_key  UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    request_key     UUID NOT NULL,
    file_key        UUID NOT NULL,          -- referencia a file_objects (MinIO)
    uploaded_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mattach_request FOREIGN KEY (request_key)
        REFERENCES connect_rural.maintenance_requests(request_key) ON DELETE CASCADE
);
```

---

## Estados de una solicitud

```
OPEN → IN_PROGRESS → RESOLVED
  ↓
CANCELLED
```

---

## Endpoints

```
# Categorías
GET    /api/{communityKey}/maintenance/categories
POST   /api/{communityKey}/maintenance/categories

# Solicitudes
GET    /api/{communityKey}/maintenance?status=OPEN&priority=HIGH
POST   /api/{communityKey}/maintenance
GET    /api/{communityKey}/maintenance/{requestKey}
PATCH  /api/{communityKey}/maintenance/{requestKey}/assign
  Body: { assignedTo }
PATCH  /api/{communityKey}/maintenance/{requestKey}/resolve
  Body: { resolutionNote }
PATCH  /api/{communityKey}/maintenance/{requestKey}/cancel

# Adjuntos
POST   /api/{communityKey}/maintenance/{requestKey}/attachments
DELETE /api/{communityKey}/maintenance/{requestKey}/attachments/{attachmentKey}

# Desde portal del residente
POST   /api/portal/maintenance               ← residente reporta
GET    /api/portal/maintenance               ← residente ve sus reportes
```

---

## Estructura de código

```
app/maintenance/
  ├── MaintenanceController.java
  └── dto/
      ├── CreateMaintenanceRequest.java
      ├── AssignMaintenanceRequest.java
      ├── ResolveMaintenanceRequest.java
      ├── MaintenanceResponse.java
      └── MaintenanceFilterRequest.java

business/maintenance/
  ├── MaintenanceService.java              ← create, assign, resolve, cancel, list, attachments
  ├── MaintenanceCategoryRepository.java   ← interfaz (puerto BD)
  ├── MaintenanceRequestRepository.java    ← interfaz (puerto BD)
  ├── MaintenanceAttachmentRepository.java ← interfaz (puerto BD)
  ├── MaintenanceSpecs.java               ← (en data/maintenance/)
  └── MaintenanceMapper.java

data/maintenance/
  ├── MaintenanceCategoryRepositoryImpl.java + JpaRepository + Entity
  ├── MaintenanceRequestRepositoryImpl.java + JpaRepository + Entity
  ├── MaintenanceAttachmentRepositoryImpl.java + JpaRepository + Entity
  └── MaintenanceSpecs.java
```

`MaintenanceService` inyecta `FileGateway` para subir adjuntos — no llama a `FileService`.

---

## Integración con otros módulos

| Integración | Cómo |
|-------------|------|
| Trabajos comunitarios | `MaintenanceService.resolve()` puede opcionalmente crear un `work_assignment` via `WorkAssignmentRepository` |
| Módulo contable | Al resolver, el admin puede registrar un `expense` vinculado a la solicitud via `ExpenseRepository` |
| Archivos (MinIO) | `MaintenanceService` inyecta `FileGateway` para subir fotos |
| Portal del residente | Residente reporta y hace seguimiento desde su portal |
| WhatsApp | `MaintenanceService` inyecta `WhatsappGateway` para notificar al residente cuando cambie estado |

---

## Orden de implementación

- [ ] 1. Migración: `maintenance_categories`, `maintenance_requests`, `maintenance_attachments`
- [ ] 2. Entidades + JpaRepositories + Repository interfaces + RepositoryImpls
- [ ] 3. DTOs + `MaintenanceMapper`
- [ ] 4. CRUD categorías — métodos en `MaintenanceService`
- [ ] 5. `MaintenanceService.create()` + `list()`
- [ ] 6. `MaintenanceService.assign()` + `resolve()`
- [ ] 7. `MaintenanceService.cancel()`
- [ ] 8. Adjuntos — integrar con `FileGateway`
- [ ] 9. Endpoint desde portal del residente
- [ ] 10. Notificación WhatsApp al cambiar estado

---

## Consideraciones

- Las solicitudes `URGENT` podrían disparar una notificación WhatsApp al admin automáticamente
- El filtro por `status` y `priority` en el listado es esencial para el dashboard del admin
- Una solicitud resuelta no debería poder reabrirse — solo crear una nueva
