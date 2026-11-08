# Arquitectura — connect-rural-api

## Patrón

**Hexagonal Architecture (Ports & Adapters)** organizada en 3 capas por módulo de dominio.

---

## Capas

```
app/        ← Adaptadores HTTP: Controllers + DTOs de entrada/salida
business/   ← Dominio: Services + interfaces Repository/Gateway + DTOs internos + enums
data/       ← Infraestructura: implementaciones JPA/HTTP + Entities + Specs
config/     ← Beans de configuración Spring
```

### app/
Recibe y responde peticiones HTTP. No contiene lógica de negocio.

- `{Module}Controller` — mapea rutas HTTP, traduce DTO ↔ modelo de dominio y delega al Service
- `{Module}Mapper` — convierte **modelo de dominio ↔ DTO HTTP** (request → dominio y dominio → response). Vive aquí y no en business/ para que business/ no dependa de app/
- `dto/{Action}{Module}Request` — DTO de entrada (`CreateCommunityRequest`, `UpdateResidentRequest`)
- `dto/{Module}Response` — DTO de salida (`CommunityResponse`, `CooperationDetailResponse`)
- `dto/{Module}PageResponse` — DTO de salida paginada
- `shared/GlobalExceptionHandler` — manejo centralizado de errores HTTP

### business/
Lógica de negocio pura. **No importa nada de `data/`, de `app/` ni de Spring Data/JPA.**

- `{Module}` — **modelo de dominio** (`Community`, `Resident`, `Cooperation`). Es lo que devuelven y reciben los puertos; nunca una `*Entity`
- `{Module}Service` — operaciones del dominio (create, update, delete, get, etc.). Devuelve modelos, no DTOs
- `{Module}Repository` — **interfaz** (puerto) para acceso a BD, definida aquí, implementada en data. Sin `Page`, `Pageable`, `Specification` ni `*Entity`
- `{Module}Query` — query object del dominio para búsquedas (`CommunityQuery{keyword}`); data/ lo traduce a `Specification`
- `{Module}Gateway` — **interfaz** (puerto) para sistemas externos (HTTP, MinIO), solo donde aplica
- `shared/` — abstracciones de dominio reutilizables: `PageParams` (page/size validados) y `PagedResult<T>` (sustituto de `Page` de Spring Data)
- Enums, calculadoras y helpers viven planos en el paquete del módulo

### data/
Implementación de infraestructura. Depende de business para implementar sus interfaces.

- `{Module}RepositoryImpl` — implementa `{Module}Repository`: traduce `*Entity` ↔ modelo, y `{Module}Query` → `Specification`, `Page` → `PagedResult`. **Único sitio donde conviven tipos de Spring Data y modelos de dominio**
- `{Module}JpaRepository` — Spring Data `JpaRepository`, solo usado por `RepositoryImpl`
- `{Module}Entity` — entidad JPA
- `{Module}EntityMapper` — mapea **entidad JPA ↔ modelo de dominio** (`CommunityEntityMapper`)
- `{Module}Specs` — JPA Specifications para filtros dinámicos
- `{Module}GatewayImpl` — implementación de `{Module}Gateway` (RestClient HTTP, MinIO client, etc.)

---

## Reglas de dependencia

```
app        →  business           ✓
business   →  data               ✗  (business solo conoce sus propias interfaces)
data       →  business           ✓  (implementa interfaces de business)
data       →  app                ✗  nunca

Controller →  Service            ✓
Controller →  {Module}Mapper     ✓  (solo para traducir DTO ↔ modelo)
Service    →  Repository (propio)     ✓
Service    →  Repository (otro módulo) ✓  (solo lectura recomendada)
Service    →  Gateway            ✓
Service    →  Service (otro)     ✗  nunca
Controller →  Repository         ✗
```

Imports prohibidos en `business/`:

| Import | Por qué |
|---|---|
| `...connect_rural_api.data.*` | El dominio no conoce JPA ni entidades |
| `...connect_rural_api.app.*` | La dependencia es unidireccional: app → business |
| `org.springframework.data.*` | Los puertos usan `PagedResult`/`PageParams`/`{Module}Query` |
| `org.springframework.web.*`, `MultipartFile` | El dominio no conoce HTTP |

---

## Migración a hexagonal estricta (Opción B)

Estado por módulo. Un módulo está migrado cuando: tiene modelo de dominio,
el puerto no expone tipos de JPA/entidades, el mapper HTTP vive en app/ y su
`*EntityMapper` vive en data/.

| Módulo | Estado |
|---|---|
| community | ✅ migrado (+ `business/shared`, + tests unitarios sin BD) |
| resident | ⬜ pendiente |
| cooperation | ⬜ pendiente |
| financialobligation | ⬜ pendiente |
| residentpayment | ⬜ pendiente |
| paymentallocation | ⬜ pendiente |
| file | ⬜ pendiente |
| whatsapp | ⬜ pendiente (sus gateways ya son puertos; falta el mapper de respuesta) |

Plantilla a replicar (módulo `community`):
`business/community/{Community,CommunityQuery,CommunityRepository,CommunityService}.java` ·
`business/shared/{PageParams,PagedResult}.java` ·
`app/community/CommunityMapper.java` ·
`data/community/{CommunityEntityMapper,CommunitySpecs,CommunityRepositoryImpl}.java` ·
`src/test/java/.../business/community/CommunityServiceTest.java` (fake en memoria, sin Spring ni H2).

---

## Estructura de paquetes

```
com.crdev.connect_rural_api/
├── ConnectRuralApiApplication.java
│
├── app/
│   ├── community/
│   │   ├── CommunityController.java
│   │   ├── CommunityMapper.java            ← modelo de dominio ↔ DTO HTTP
│   │   └── dto/
│   │       ├── CreateCommunityRequest.java
│   │       ├── UpdateCommunityRequest.java
│   │       ├── CommunityFilterRequest.java
│   │       ├── CommunityResponse.java
│   │       └── CommunityPageResponse.java
│   ├── resident/
│   │   ├── ResidentController.java
│   │   └── dto/
│   ├── cooperation/
│   │   ├── CooperationController.java
│   │   └── dto/
│   ├── file/
│   │   ├── FileController.java
│   │   └── dto/
│   ├── whatsapp/
│   │   ├── WhatsappCallbackController.java
│   │   └── dto/
│   └── shared/
│       └── GlobalExceptionHandler.java
│
├── business/
│   ├── shared/                             ← abstracciones de dominio
│   │   ├── PageParams.java                 ← page/size validados (sustituye a Pageable)
│   │   └── PagedResult.java                ← resultado paginado (sustituye a Page)
│   ├── community/
│   │   ├── Community.java                  ← modelo de dominio (sustituye a CommunityEntity)
│   │   ├── CommunityService.java
│   │   ├── CommunityRepository.java        ← interfaz (puerto BD), sin tipos JPA
│   │   └── CommunityQuery.java             ← query object del dominio
│   ├── resident/
│   │   ├── ResidentService.java
│   │   └── ResidentRepository.java
│   ├── cooperation/
│   │   ├── CooperationService.java
│   │   ├── CooperationRepository.java
│   │   ├── CooperationAssignmentType.java  ← enum plano en el módulo
│   │   ├── LateFeeCalculator.java
│   │   └── dto/
│   ├── cooperationresident/               ← paquetes en lowercase, sin camelCase
│   │   ├── CooperationResidentService.java
│   │   └── CooperationResidentRepository.java
│   ├── financialobligation/
│   │   ├── FinancialObligationService.java
│   │   └── FinancialObligationRepository.java
│   ├── residentpayment/
│   │   ├── ResidentPaymentService.java
│   │   └── ResidentPaymentRepository.java
│   ├── file/
│   │   ├── FileService.java
│   │   ├── FileRepository.java
│   │   ├── FileGateway.java               ← interfaz (puerto MinIO)
│   │   └── dto/
│   └── whatsapp/
│       ├── WhatsappService.java
│       └── WhatsappGateway.java           ← interfaz (puerto gateway HTTP externo)
│
├── data/
│   ├── community/
│   │   ├── CommunityRepositoryImpl.java   ← implements CommunityRepository (Entity ↔ dominio)
│   │   ├── CommunityJpaRepository.java    ← Spring Data
│   │   ├── CommunityEntity.java
│   │   ├── CommunityEntityMapper.java     ← entidad JPA ↔ modelo de dominio
│   │   └── CommunitySpecs.java
│   ├── resident/
│   │   ├── ResidentRepositoryImpl.java
│   │   ├── ResidentJpaRepository.java
│   │   ├── ResidentEntity.java
│   │   └── ResidentSpecs.java
│   ├── cooperation/
│   │   ├── CooperationRepositoryImpl.java
│   │   ├── CooperationJpaRepository.java
│   │   ├── CooperationEntity.java
│   │   └── CooperationSpecs.java
│   ├── cooperationresident/
│   │   ├── CooperationResidentRepositoryImpl.java
│   │   ├── CooperationResidentJpaRepository.java
│   │   └── CooperationResidentEntity.java
│   ├── financialobligation/
│   │   ├── FinancialObligationRepositoryImpl.java
│   │   ├── FinancialObligationJpaRepository.java
│   │   └── FinancialObligationEntity.java
│   ├── residentpayment/
│   │   ├── ResidentPaymentRepositoryImpl.java
│   │   ├── ResidentPaymentJpaRepository.java
│   │   └── ResidentPaymentEntity.java
│   ├── paymentallocation/
│   │   ├── PaymentAllocationRepositoryImpl.java
│   │   ├── PaymentAllocationJpaRepository.java
│   │   └── PaymentAllocationEntity.java
│   ├── file/
│   │   ├── FileRepositoryImpl.java
│   │   ├── FileGatewayImpl.java           ← MinIO client
│   │   ├── FileJpaRepository.java
│   │   ├── FileObjectEntity.java
│   │   └── FileStatus.java
│   └── whatsapp/
│       └── WhatsappGatewayImpl.java       ← RestClient HTTP
│
└── config/
    ├── CorsConfig.java
    ├── MinioConfig.java
    └── WhatsappGatewayConfig.java
```

---

## Naming conventions

| Artefacto | Patrón | Ejemplo |
|---|---|---|
| Controlador | `{Module}Controller` | `CooperationController` |
| Servicio | `{Module}Service` | `CooperationService` |
| Modelo de dominio | `{Module}` en `business/{module}/` | `Community` |
| Puerto BD (interfaz) | `{Module}Repository` en `business/{module}/` | `CooperationRepository` |
| Puerto externo (interfaz) | `{Module}Gateway` | `WhatsappGateway` |
| Query object | `{Module}Query` en `business/{module}/` | `CommunityQuery` |
| Paginación de dominio | `PageParams`, `PagedResult` en `business/shared/` | `PagedResult<Community>` |
| Mapper HTTP (dominio ↔ DTO) | `{Module}Mapper` en `app/{module}/` | `CommunityApiMapper` |
| Mapper entidad (JPA ↔ dominio) | `{Module}EntityMapper` en `data/{module}/` | `CommunityEntityMapper` |
| Adaptador BD | `{Module}RepositoryImpl` en `data/{module}/` | `CooperationRepositoryImpl` |
| Adaptador externo | `{Module}GatewayImpl` | `WhatsappGatewayImpl` |
| Spring Data | `{Module}JpaRepository` | `CooperationJpaRepository` |
| Entidad JPA | `{Module}Entity` | `CooperationEntity` |
| Specs JPA | `{Module}Specs` | `CooperationSpecs` |
| DTO entrada HTTP | `{Action}{Module}Request` | `CreateCooperationRequest` |
| DTO salida HTTP | `{Module}Response` | `CooperationResponse` |
| DTO salida paginada | `{Module}PageResponse` | `CooperationPageResponse` |
| Enum | `{Concept}` plano en el módulo | `CooperationAssignmentType` |
| Paquetes | todo lowercase, sin camelCase | `cooperationresident/` |

---

## Módulos de dominio

| Módulo | Rutas | Controller |
|---|---|---|
| community | `/api/communities` | ✓ |
| resident | `/api/{communityKey}/residents` | ✓ |
| cooperation | `/api/{communityKey}/cooperations` | ✓ |
| cooperationresident | — (sin HTTP propio) | ✗ |
| financialobligation | — (sin HTTP propio) | ✗ |
| residentpayment | — (sin HTTP propio) | ✗ |
| file | `/api/files` | ✓ |
| whatsapp | `/api/whatsapp/events` (callback) | ✓ |
