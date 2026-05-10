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

- `{Module}Controller` — mapea rutas HTTP, delega al Service
- `dto/{Action}{Module}Request` — DTO de entrada (`CreateCommunityRequest`, `UpdateResidentRequest`)
- `dto/{Module}Response` — DTO de salida (`CommunityResponse`, `CooperationDetailResponse`)
- `dto/{Module}PageResponse` — DTO de salida paginada
- `shared/GlobalExceptionHandler` — manejo centralizado de errores HTTP

### business/
Lógica de negocio pura. No importa nada de Spring Data ni JPA directamente.

- `{Module}Service` — operaciones del dominio (create, update, delete, get, etc.)
- `{Module}Repository` — **interfaz** (puerto) para acceso a BD, definida aquí, implementada en data
- `{Module}Gateway` — **interfaz** (puerto) para sistemas externos (HTTP, MinIO), solo donde aplica
- `dto/{Module}Model` — modelo interno de dominio, solo si se necesita separar del DTO HTTP
- Enums, calculadoras y helpers viven planos en el paquete del módulo

### data/
Implementación de infraestructura. Depende de business para implementar sus interfaces.

- `{Module}RepositoryImpl` — implementa `{Module}Repository` usando JPA
- `{Module}JpaRepository` — Spring Data `JpaRepository`, solo usado por `RepositoryImpl`
- `{Module}Entity` — entidad JPA
- `{Module}Specs` — JPA Specifications para filtros dinámicos
- `{Module}GatewayImpl` — implementa `{Module}Gateway` (RestClient HTTP, MinIO client, etc.)

---

## Reglas de dependencia

```
app        →  business           ✓
business   →  data               ✗  (business solo conoce sus propias interfaces)
data       →  business           ✓  (implementa interfaces de business)

Controller →  Service            ✓
Service    →  Repository (propio)     ✓
Service    →  Repository (otro módulo) ✓  (solo lectura recomendada)
Service    →  Gateway            ✓
Service    →  Service (otro)     ✗  nunca
Controller →  Repository         ✗
```

---

## Estructura de paquetes

```
com.crdev.connect_rural_api/
├── ConnectRuralApiApplication.java
│
├── app/
│   ├── community/
│   │   ├── CommunityController.java
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
│   ├── community/
│   │   ├── CommunityService.java
│   │   ├── CommunityRepository.java        ← interfaz (puerto BD)
│   │   └── dto/
│   ├── resident/
│   │   ├── ResidentService.java
│   │   ├── ResidentRepository.java
│   │   └── dto/
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
│   │   ├── CommunityRepositoryImpl.java   ← implements CommunityRepository
│   │   ├── CommunityJpaRepository.java    ← Spring Data
│   │   ├── CommunityEntity.java
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
| Puerto BD (interfaz) | `{Module}Repository` | `CooperationRepository` |
| Puerto externo (interfaz) | `{Module}Gateway` | `WhatsappGateway` |
| Adaptador BD | `{Module}RepositoryImpl` | `CooperationRepositoryImpl` |
| Adaptador externo | `{Module}GatewayImpl` | `WhatsappGatewayImpl` |
| Spring Data | `{Module}JpaRepository` | `CooperationJpaRepository` |
| Entidad JPA | `{Module}Entity` | `CooperationEntity` |
| Specs JPA | `{Module}Specs` | `CooperationSpecs` |
| DTO entrada HTTP | `{Action}{Module}Request` | `CreateCooperationRequest` |
| DTO salida HTTP | `{Module}Response` | `CooperationResponse` |
| DTO salida paginada | `{Module}PageResponse` | `CooperationPageResponse` |
| Modelo interno | `{Module}Model` | `CooperationModel` |
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
