# Arquitectura actual — Connect Rural API

> **Estado de esta documentación:** describe la estructura observada en la rama `develop` al 21 de noviembre de 2026. La migración arquitectónica está en curso. Este documento separa la arquitectura implementada del estado de migración; no presupone que todos los dominios sigan ya un único patrón.

## 1. Vista general

El repositorio usa Maven multimódulo con Java 21 y Spring Boot 3.5.0. El POM raíz declara cuatro módulos:

```text
connect-rural-api-parent
├── models
├── repositories
├── services
└── api
```

La separación Maven actual es:

| Módulo | Responsabilidad observada | Dependencias internas |
|---|---|---|
| `models` | Entidades JPA compartidas bajo `com.crdev.connect_rural_api.model`. | Ninguna de los otros módulos del proyecto. |
| `repositories` | Repositorios Spring Data JPA y consultas/especificaciones de persistencia. | `models`. |
| `services` | Servicios y modelos/consultas de negocio para dominios que siguen usando esta implementación, además de servicios de almacenamiento. | `repositories`, `models`. |
| `api` | Aplicación Spring Boot, controladores HTTP, DTOs, configuración, migraciones Liquibase y código de dominios que ya tiene paquetes `app/`, `business/` y `data/`. | `models`, `services`. |

Dependencias Maven entre módulos:

```text
models
  ↑
repositories
  ↑
services
  ↑
api

api ───────────────→ models
services ──────────→ models
```

El esquema representa las dependencias de módulos declaradas en los POM; no sustituye el análisis de dependencias entre paquetes Java. El POM raíz también declara dependencias comunes, por lo que su disponibilidad no implica que cada módulo deba utilizarlas directamente.

## 2. Estructura del código

### `models/`

Contiene entidades JPA compartidas, actualmente en `com.crdev.connect_rural_api.model`. Entre ellas están `CommunityEntity`, `ResidentEntity`, `CooperationEntity`, `FinancialObligationEntity` y entidades relacionadas con pagos.

### `repositories/`

Contiene interfaces de persistencia y consultas bajo `com.crdev.connect_rural_api.repository`, agrupadas por dominio, por ejemplo `community`, `resident`, `cooperation` y `payment`.

### `services/`

Contiene implementaciones de servicios y modelos/consultas de negocio bajo `com.crdev.connect_rural_api.service`. Comunidades y residentes usan actualmente este módulo. También existen servicios compartidos de paginación y almacenamiento.

### `api/`

La clase de entrada es:

`api/src/main/java/com/crdev/connect_rural_api/ConnectRuralApiApplication.java`

En el código de API coexisten paquetes con responsabilidades distintas:

- `app/`: controladores HTTP, DTOs y mappers de entrada/salida.
- `business/`: servicios, interfaces de repositorio/gateway y lógica asociada a ciertos dominios.
- `data/`: adaptadores de persistencia y de infraestructura, incluidos repositorios JPA e implementaciones de puertos.
- `config/`: configuración de la aplicación.
- `src/main/resources/db/changelog/`: changelog y scripts de migración Liquibase.

**Importante:** la existencia de paquetes `app/`, `business/` y `data/` dentro de `api` no significa que todos los dominios estén migrados a esa estructura. En la rama revisada, los controladores de comunidades y residentes inyectan servicios de `com.crdev.connect_rural_api.service.*`, ubicados en el módulo Maven `services`.

## 3. Estado de migración por dominio

La siguiente tabla describe lo que se observa en el código; que una funcionalidad esté operativa no implica que su migración arquitectónica esté terminada.

| Dominio | Implementación observada | Lectura del estado |
|---|---|---|
| Comunidades | Controlador y DTOs en `api/app/community`; servicio y modelo de negocio en `services/service/community`; persistencia compartida en `repositories` y `models`. | Funcionalidad existente; todavía usa el flujo de módulos `services/repositories/models`. |
| Residentes | Controlador y DTOs en `api/app/resident`; servicio y modelos de negocio en `services/service/resident`; persistencia compartida en `repositories` y `models`. | Funcionalidad existente; todavía usa el flujo de módulos `services/repositories/models`. |
| Cooperaciones y pagos relacionados | Hay código en `api/app`, `api/business` y `api/data`, además de clases relacionadas en los módulos `models` y `repositories`. | Revisar el flujo efectivo por operación antes de declarar migración completa o retirar implementaciones anteriores. |
| Archivos/almacenamiento | Hay componentes en `api/business`/`api/data` y un servicio de almacenamiento en `services`. | Coexisten responsabilidades; documentar los puntos de entrada y el adaptador activo antes de cambiarlo. |
| WhatsApp | Hay componentes de integración y configuración en la API, además de servicios/gateways según el árbol de código. | Confirmar el flujo activo por endpoint antes de marcar la migración como terminada. |

Esta matriz debe actualizarse cuando un dominio cambie de módulo o cuando se retire una implementación anterior.

## 4. Flujo de una petición

Para comunidades y residentes, el flujo observado es:

```text
HTTP
  ↓
api/app/{community|resident}/*Controller
  ↓
services/service/{community|resident}/*Service
  ↓
repositories/repository/*Repository
  ↓
models/model/*Entity
  ↓
PostgreSQL (esquema connect_rural)
```

Los nombres exactos de repositorio y entidad dependen del dominio. Los controladores traducen entre DTO HTTP y modelos del módulo `services`; no se debe asumir que el flujo de todos los dominios es idéntico.

Para los dominios que usan `api/business` y `api/data`, debe trazarse el flujo desde el controlador hasta la implementación de persistencia correspondiente. No se debe inferir su estado únicamente por la ubicación de las clases.

## 5. Base de datos y configuración

- Base de datos PostgreSQL configurada mediante `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD`.
- Esquema de aplicación: `connect_rural`.
- Migraciones administradas por Liquibase desde `api/src/main/resources/db/changelog/db.changelog-master.xml`.
- Hibernate tiene `spring.jpa.hibernate.ddl-auto=none`; los cambios de esquema deben gestionarse con migraciones.
- La aplicación integra almacenamiento MinIO/S3 y un gateway de WhatsApp; revisar la configuración de cada integración antes de añadir otra.

Los scripts de migración deben respetar las convenciones de esquema y claves UUID definidas por los archivos de migración existentes y las instrucciones vigentes del repositorio.

## 6. Reglas para continuar la migración

1. **No refactorizar por apariencia.** Mantener el comportamiento existente y migrar un dominio de forma explícita, con pruebas.
2. **Trazar el flujo real antes de modificarlo.** Identificar controlador, servicio, repositorio, entidad y dependencias que realmente usa cada operación.
3. **Evitar dos fuentes de comportamiento.** Si un dominio se migra, definir qué implementación queda activa y cuándo puede retirarse la anterior.
4. **Mantener contratos HTTP y de persistencia.** Cualquier cambio en rutas, DTOs o esquema debe ser intencional y probado.
5. **No declarar migración completa por la existencia de paquetes.** El estado debe basarse en dependencias reales y pruebas.
6. **Actualizar esta documentación con cada migración relevante.** Registrar el flujo nuevo, las clases retiradas y las pruebas ejecutadas.
7. **No asumir que la arquitectura objetivo ya está decidida por este documento.** Antes de introducir un patrón nuevo o mover dominios, acordar la convención con el equipo y reflejarla en las instrucciones de desarrollo.

## 7. Verificación

Los comandos definidos por el proyecto son:

```bash
mvn clean verify
mvn test
```

La revisión de fuentes y POMs no sustituye la ejecución de estos comandos. No se debe afirmar que el build o los tests pasan sin ejecutarlos y comprobar el resultado.

## 8. Pendientes de arquitectura

- Consultar la [auditoría de arquitectura y migración](auditoria-arquitectura-2026-11.md), que documenta hallazgos y prioridades observados en `develop`.
- Acordar y documentar el patrón objetivo para nuevos dominios y para las migraciones de los dominios intermedios.
- Completar la matriz de migración con evidencias de pruebas y las implementaciones que se retiren.
- Documentar los límites de dependencias permitidos entre paquetes y añadir pruebas arquitectónicas si el equipo decide adoptar reglas estrictas.
- Corregir el fallo de CI documentado en la auditoría antes de usar `mvn clean verify` como señal de validación de las siguientes migraciones.
