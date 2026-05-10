# Plan: Migración de Arquitectura

## Objetivo

Migrar el código existente a **Hexagonal Architecture (Ports & Adapters)** en 3 capas, eliminando el patrón Use Case por clase, aplanando paquetes de DTOs y estableciendo Repository interfaces como puertos entre business y data.

Documentación de referencia: [`docs/architecture.md`](architecture.md)

---

## Resumen de cambios

| Antes | Después |
|---|---|
| `UseCase` por operación | Método en `{Module}Service` |
| `dto/request/` + `dto/response/` | `dto/` plano |
| `*Dto` suffix | `*Request` / `*Response` |
| `mapper/` sub-paquete | `{Module}Mapper` plano en el módulo |
| `specs/` sub-paquete en business | `{Module}Specs` en `data/` |
| `{Module}Repository` → Spring Data | `{Module}JpaRepository` → Spring Data (solo en data) |
| Service inyecta JpaRepository directo | Service inyecta `{Module}Repository` (interfaz) |
| Paquetes en camelCase | Paquetes en lowercase |
| `app/controllers/` y `app/exceptions/` | `app/shared/` |

---

## Fase 1 — app layer: aplanar y renombrar DTOs

### 1.1 Aplanar sub-paquetes

Para cada módulo en `app/`: mover todo lo de `dto/request/` y `dto/response/` directamente a `dto/`.

### 1.2 Renombrar archivos

#### community

| Antes | Después |
|---|---|
| `CommunityFilterDto` | `CommunityFilterRequest` |
| `CreateCommunityDto` | `CreateCommunityRequest` |
| `RegisterWhatsappTenantDto` | `RegisterWhatsappTenantRequest` |
| `CommunityAdminResponseDto` | `CommunityAdminResponse` |
| `CommunityPaginatedResponseDto` | `CommunityPageResponse` |
| `CommunityResponseDto` | `CommunityResponse` |

#### resident

| Antes | Después |
|---|---|
| `CreateResidentDto` | `CreateResidentRequest` |
| `ResidentFilterDto` | `ResidentFilterRequest` |
| `ResidentDetailResponseDto` | `ResidentDetailResponse` |
| `ResidentPaginatedResponseDto` | `ResidentPageResponse` |
| `ResidentResponseDto` | `ResidentResponse` |
| `SimpleResidentResponseDto` | `SimpleResidentResponse` |

#### cooperation

| Antes | Después |
|---|---|
| `CooperationFilterDto` | `CooperationFilterRequest` |
| `CreateCooperationRequestDto` | `CreateCooperationRequest` |
| `MarkAsPaidRequestDto` | `MarkAsPaidRequest` |
| `CooperationDetailResponseDto` | `CooperationDetailResponse` |
| `CooperationResponseDto` | `CooperationResponse` |
| `CooperationSummaryPaginatedResponseDto` | `CooperationPageResponse` |
| `CooperationSummaryResponseDto` | `CooperationSummaryResponse` |

#### whatsapp

| Antes | Después |
|---|---|
| `GatewayEventDto` | `GatewayEventRequest` |
| `GatewayMessageDto` | `GatewayMessageRequest` |
| `GatewayStatusDto` | `GatewayStatusRequest` |
| `SendWhatsappMessageDto` | `SendWhatsappMessageRequest` |

#### file

| Antes | Después |
|---|---|
| `FileResponseDto` | `FileResponse` |

### 1.3 Mover HealthController y GlobalExceptionHandler

```
app/controllers/HealthController.java     → app/shared/HealthController.java
app/exceptions/GlobalExceptionHandler.java → app/shared/GlobalExceptionHandler.java
```

---

## Fase 2 — business layer: mappers y specs

### 2.1 Mover y renombrar Mappers (quitar sub-paquete `mapper/`)

```
business/community/mapper/CommunityAppMapper    → business/community/CommunityMapper
business/cooperation/mapper/CooperationAppMapper → business/cooperation/CooperationMapper
business/resident/mapper/ResidentAppMapper       → business/resident/ResidentMapper
business/file/mapper/FileAppMapper               → business/file/FileMapper
```

### 2.2 Mover Specs de business a data

```
business/cooperation/specs/CooperationSpecs → data/cooperation/CooperationSpecs
business/resident/specs/ResidentSpecs       → data/resident/ResidentSpecs
```

---

## Fase 3 — Introducir Repository interfaces (puertos)

Este es el cambio estructural más importante. Para cada módulo:

1. Crear interfaz `{Module}Repository` en `business/{module}/`
2. Renombrar Spring Data repo: `{Module}Repository` → `{Module}JpaRepository` en `data/{module}/`
3. Crear `{Module}RepositoryImpl` en `data/{module}/` que implementa la interfaz y usa `JpaRepository`

### Módulos y archivos

#### community

```java
// NUEVO: business/community/CommunityRepository.java
public interface CommunityRepository {
    Optional<CommunityEntity> findByKey(UUID key);
    Page<CommunityEntity> findAll(Specification<CommunityEntity> spec, Pageable pageable);
    CommunityEntity save(CommunityEntity entity);
    // ... métodos necesarios
}

// RENOMBRAR: data/community/CommunityRepository → CommunityJpaRepository
// NUEVO: data/community/CommunityRepositoryImpl implements CommunityRepository
//   inyecta CommunityJpaRepository, delega
```

Aplica igual para: **resident**, **cooperation**, **cooperationresident**, **financialobligation**, **residentpayment**, **paymentallocation**, **file**

### 3.2 Extraer WhatsappGateway

```
business/whatsapp/WhatsappGateway.java            ← NUEVA interfaz (puerto)
data/whatsapp/WhatsappGatewayImpl.java            ← MOVER WhatsappGatewayService aquí
```

`WhatsappGatewayImpl` contiene la lógica de RestClient HTTP. `WhatsappService` en business inyecta `WhatsappGateway`.

### 3.3 Extraer FileGateway

```
business/file/FileGateway.java                    ← NUEVA interfaz (puerto MinIO)
data/file/FileGatewayImpl.java                    ← implementación MinIO (era FileStorageService)
```

---

## Fase 4 — Fusionar Use Cases en Services

Para cada módulo, mover la lógica de cada `UseCase.execute()` como método nombrado en el `Service`. Luego eliminar la clase `UseCase`.

### community

| Clase eliminada | Método en `CommunityService` |
|---|---|
| `CreateCommunityUseCase` | `create(CreateCommunityRequest)` |
| `UpdateCommunityUseCase` | `update(UUID key, UpdateCommunityRequest)` |
| `DeleteCommunityUseCase` | `delete(UUID key)` |
| `GetCommunityByKeyUseCase` | `getByKey(UUID key)` |
| `GetCommunityListUseCase` | `getList()` |
| `GetCommunityPaginatedUseCase` | `getPaginated(CommunityFilterRequest, Pageable)` |

### resident

| Clase eliminada | Método en `ResidentService` |
|---|---|
| `CreateResidentUseCase` | `create(UUID communityKey, CreateResidentRequest)` |
| `UpdateResidentUseCase` | `update(UUID key, UpdateResidentRequest)` |
| `DeleteResidentUseCase` | `delete(UUID key)` |
| `GetResidentByKeyUseCase` | `getByKey(UUID key)` |
| `GetResidentPaginatedUseCase` | `getPaginated(UUID communityKey, ResidentFilterRequest, Pageable)` |
| `GetResidentListByCommunityKeyUseCase` | `listByCommunity(UUID communityKey)` |
| `GetResidentsCatalogUseCase` | `getCatalog(UUID communityKey)` |

### cooperation

| Clase eliminada | Método en `CooperationService` |
|---|---|
| `CreateCooperationUseCase` | `create(UUID communityKey, CreateCooperationRequest)` |
| `UpdateCooperationUseCase` | `update(UUID key, UpdateCooperationRequest)` |
| `DeleteCooperationUseCase` | `delete(UUID key)` |
| `CloseCooperationUseCase` | `close(UUID key)` |
| `ReopenCooperationUseCase` | `reopen(UUID key)` |
| `GetCooperationByKeyUseCase` | `getByKey(UUID key)` |
| `GetCooperationDetailByKeyUseCase` | `getDetail(UUID key)` |
| `GetCooperationListUseCase` | `getList(UUID communityKey)` |
| `GetCooperationPaginatedUseCase` | `getPaginated(UUID communityKey, CooperationFilterRequest, Pageable)` |
| `MarkAsPaidUseCase` | `markAsPaid(UUID cooperationKey, UUID residentKey, MarkAsPaidRequest)` |
| `MarkAsUnpaidUseCase` | `markAsUnpaid(UUID cooperationKey, UUID residentKey)` |
| `MarkAllAsPaidUseCase` | `markAllAsPaid(UUID cooperationKey)` |

### whatsapp

| Clase eliminada | Método en `WhatsappService` |
|---|---|
| `ProcessGatewayEventUseCase` | `processEvent(GatewayEventRequest)` |
| `SendWhatsappMessageUseCase` | `sendMessage(String appKey, String to, String text)` |
| `RegisterCommunityTenantUseCase` | `registerTenant(UUID communityKey, RegisterWhatsappTenantRequest)` |
| `UnlinkCommunityTenantUseCase` | `unlinkTenant(UUID communityKey)` |
| `NotifyResidentUseCase` | `notifyResident(UUID communityKey, UUID residentKey, String message)` |

### file

| Clase eliminada | Método en `FileService` |
|---|---|
| `UploadFileUseCase` | `upload(MultipartFile file, String folder)` |
| `DownloadFileUseCase` | `download(UUID fileKey)` |
| `DeleteFileUseCase` | `delete(UUID fileKey)` |
| `GetFileMetadataUseCase` | `getMetadata(UUID fileKey)` |
| `ListFilesUseCase` | `list(String folder)` |

---

## Fase 5 — Actualizar Controllers

Reemplazar inyección de `UseCase` por inyección del `Service` correspondiente. Actualizar cada llamada `useCase.execute(...)` → `service.methodName(...)`.

Módulos con controller: **community**, **resident**, **cooperation**, **file**, **whatsapp**

---

## Fase 6 — Renombrar paquetes camelCase a lowercase

```
business/cooperationResident/  → business/cooperationresident/
business/financialObligation/  → business/financialobligation/
business/residentPayment/      → business/residentpayment/
data/cooperationResident/      → data/cooperationresident/
data/financialObligation/      → data/financialobligation/
data/residentPayment/          → data/residentpayment/
data/paymentAllocation/        → data/paymentallocation/
```

---

## Fase 7 — Limpieza final

- [ ] Eliminar todas las clases `*UseCase.java`
- [ ] Eliminar sub-paquetes `usecases/`, `mapper/`, `specs/` (en business), `enums/` (mover enum plano al módulo)
- [ ] Eliminar sub-paquetes `dto/request/` y `dto/response/` en app
- [ ] Verificar que ningún `Service` inyecte otro `Service` — solo `Repository` o `Gateway`
- [ ] Verificar que ninguna clase de `business/` importe de `data/` (solo interfaces)
- [ ] Ejecutar `mvn clean verify` — todos los tests deben pasar

---

## Orden de ejecución recomendado

```
Fase 1 (app DTOs)         → bajo riesgo, sin lógica
Fase 2 (mappers, specs)   → mover archivos, actualizar imports
Fase 3 (Repository ports) → crear interfaces, impl; no eliminar nada aún
Fase 4 (merge use cases)  → mover lógica al Service, pruebas parciales
Fase 5 (controllers)      → actualizar inyecciones
Fase 6 (paquetes)         → refactor de nombres
Fase 7 (limpieza)         → eliminar clases vacías
```

---

## Checklist

- [ ] Fase 1 — DTOs aplanados y renombrados
- [ ] Fase 2 — Mappers y Specs reubicados
- [ ] Fase 3 — Repository interfaces creadas en business
- [ ] Fase 3 — JpaRepositories renombrados en data
- [ ] Fase 3 — RepositoryImpl creados en data
- [ ] Fase 3 — WhatsappGateway extraído
- [ ] Fase 3 — FileGateway extraído
- [ ] Fase 4 — Use cases fusionados en Services
- [ ] Fase 5 — Controllers actualizados
- [ ] Fase 6 — Paquetes renombrados a lowercase
- [ ] Fase 7 — Clases eliminadas
- [ ] Fase 7 — `mvn clean verify` pasa sin errores
