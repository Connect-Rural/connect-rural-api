# Auditoría de arquitectura — Connect Rural API

**Fecha:** 22 de noviembre de 2026  
**Rama revisada:** `develop`  
**Commit revisado:** `02b06130e11d20df576a9fb8a0d3d287b4034859`  
**Alcance:** estado de migración Maven multimódulo y flujos de dominios. Esta revisión es estática: se inspeccionaron árbol de archivos, POMs, documentación y clases representativas; no se ejecutó el proyecto localmente.

## Resumen ejecutivo

La migración está en un estado mixto y progresivo. Comunidades y residentes usan el flujo de los módulos `api → services → repositories → models`. Cooperaciones, obligaciones financieras, pagos y archivos usan en gran medida clases ubicadas dentro de `api/business` y `api/data`, aunque parte de su persistencia y entidades ya se comparte con `repositories` y `models`.

**Bloqueador verificado:** el workflow de GitHub Actions posterior al merge de PR #22 falló al empaquetar `connect-rural-models`: Spring Boot Maven Plugin intentó ejecutar `repackage` y no encontró una clase principal. El workflow de pruebas quedó omitido porque el trabajo de build falló. Esto no demuestra que el PR documental haya introducido el problema; sí demuestra que `develop` no está verde en esa ejecución.

## Hallazgos

### A-01 — El empaquetado del módulo `models` falla en CI

- **Prioridad:** P0 — bloquea el build de CI.
- **Evidencia:** ejecución [GitHub Actions #44971185910](https://github.com/Connect-Rural/connect-rural-api/actions/runs/44971185910), job [Build project](https://github.com/Connect-Rural/connect-rural-api/actions/runs/44971185910/job/137051094986).
- **Error observado:** `spring-boot-maven-plugin:3.5.0:repackage` falla en `connect-rural-models` con `Unable to find main class`.
- **Causa probable respaldada por configuración:** el POM raíz declara `spring-boot-maven-plugin` en `build/plugins`; por herencia, el plugin se ejecuta en el módulo de entidades, que no es una aplicación ejecutable. Los POMs de los módulos heredan la configuración raíz.
- **Recomendación:** limitar el plugin de Spring Boot al módulo `api`, o desactivar explícitamente su herencia/ejecución en los módulos de librería (`models`, `repositories`, `services`). Ejecutar después `mvn clean verify` y comprobar que el job de pruebas también se ejecuta.
- **Estado:** confirmado en CI; la corrección no forma parte de esta auditoría.

### A-02 — Comunidades y residentes siguen el patrón multimódulo actual

- **Prioridad:** informativo / referencia positiva.
- **Evidencia:** controladores en `api/src/main/java/com/crdev/connect_rural_api/app/community` y `app/resident`; servicios en `services/.../service/community` y `service/resident`; repositorios en `repositories/.../repository/community` y `repository/resident`; entidades en `models/.../model`.
- **Lectura:** son los mejores ejemplos disponibles para mantener el flujo funcional durante la migración. No se recomienda refactorizarlos solo por uniformidad.

### A-03 — Cooperaciones está parcialmente migrado y cruza límites de capa

- **Prioridad:** P1 — deuda arquitectónica.
- **Evidencia:** `CooperationController` está en `api/app/cooperation`; `CooperationService`, `CooperationRepository` y reglas de negocio permanecen en `api/business/cooperation`; la implementación JPA y las especificaciones están en `api/data/cooperation`; la entidad está en `models`.
- **Cruces observados:** `CooperationService` importa DTOs HTTP desde `api/app/cooperation/dto`, especificaciones desde `api/data/cooperation` y un repositorio de residentes desde el módulo `repositories`.
- **Riesgo:** la lógica de negocio depende de tipos de presentación y de detalles de infraestructura, dificultando extraer el dominio sin arrastrar esas dependencias.
- **Recomendación:** al migrar este dominio, definir primero contratos de servicio independientes de HTTP y mantener especificaciones/implementaciones de persistencia detrás de interfaces; mover una operación a la vez con pruebas de regresión.

### A-04 — Obligaciones financieras y pagos están en una estructura intermedia

- **Prioridad:** P1 — deuda arquitectónica.
- **Evidencia:** `FinancialObligationService`, `ResidentPaymentService` y las interfaces de `FinancialObligationRepository`, `ResidentPaymentRepository` y `PaymentAllocationRepository` están bajo `api/business`; las implementaciones JPA están bajo `api/data`; entidades en `models`. También existen interfaces de repositorio en el módulo `repositories`.
- **Riesgo:** coexisten contratos de persistencia entre `api/business` y `repositories`; antes de mover clases hay que comprobar qué interfaz consume cada servicio para evitar duplicar repositorios o cambiar el comportamiento.
- **Recomendación:** documentar la matriz servicio → interfaz → implementación activa por operación. Consolidar un contrato por agregado y migrar con pruebas transaccionales de pago, asignación y reversión.

### A-05 — Archivos expone tipos de infraestructura a las capas superiores

- **Prioridad:** P1 — deuda arquitectónica; revisar además el alcance de acceso.
- **Evidencia:** `FileController` importa `FileObjectEntity` de `api/data/file`; `FileService` usa `FileObjectEntity` y `FileStatus`; `FileRepository` devuelve/recibe `FileObjectEntity` y depende de `FileStatus`, ambos del paquete `data`.
- **Riesgo:** el contrato de negocio está ligado al adaptador de persistencia. Además, las rutas visibles son `/api/files` sin `communityKey`; esta auditoría no determina si existe control de acceso efectivo en otra capa, así que debe verificarse antes de considerar el endpoint seguro para datos comunitarios.
- **Recomendación:** separar metadatos/contratos del dominio de la entidad de persistencia; verificar autorización y pertenencia comunitaria para listar, descargar y eliminar archivos antes de modificar rutas o contratos.

### A-06 — Almacenamiento tiene componentes en dos módulos

- **Prioridad:** P2 — aclaración de responsabilidades.
- **Evidencia:** existe `services/service/storage/S3StorageService` y `StorageService`, además de `api/business/file/FileService`, `api/business/file/FileGateway`, `api/data/file/FileGatewayImpl` y `api/config/MinioConfig`.
- **Riesgo:** la ubicación de clases sugiere dos abstracciones de almacenamiento que deben distinguirse; el árbol por sí solo no demuestra que sean implementaciones duplicadas ni que ambas estén activas.
- **Recomendación:** trazar inyecciones y usos reales, decidir si representan responsabilidades distintas o una única capacidad, y documentar el adaptador activo antes de unificar nada.

### A-07 — Integración de WhatsApp requiere trazar el flujo completo

- **Prioridad:** P2 — pendiente de verificación.
- **Evidencia:** se observa `WhatsappGatewayConfig` y un DTO para registrar un tenant desde el área de comunidades, pero no aparece un paquete de dominio WhatsApp completo en el árbol Java revisado.
- **Riesgo:** no se puede declarar la migración completa ni determinar el punto de entrada activo solo con la configuración.
- **Recomendación:** localizar el endpoint que registra/configura tenants, el cliente HTTP que llama al gateway, los modelos de petición/respuesta y las pruebas existentes antes de decidir cambios de estructura.

### A-08 — La sección de pendientes de `docs/architecture.md` quedó desactualizada

- **Prioridad:** P2 — documentación.
- **Evidencia:** la sección 8 todavía pide sincronizar `AGENTS.md`, `CLAUDE.md` y las skills, aunque en `develop` ya contienen la guía de migración progresiva.
- **Recomendación:** sustituir ese pendiente por un enlace a esta auditoría y mantener como pendientes el acuerdo del patrón objetivo, los límites de dependencias y las pruebas arquitectónicas que el equipo decida adoptar.

## Orden recomendado de trabajo

1. **Restablecer CI** corrigiendo la ejecución heredada de `spring-boot-maven-plugin` en módulos de librería. No empezar migraciones con el build roto.
2. **Agregar/confirmar pruebas de regresión** para los flujos existentes y revisar que CI ejecute tanto compilación como pruebas.
3. **Trazar archivos y almacenamiento**, incluido el alcance de autorización por comunidad, antes de mover código.
4. **Migrar obligaciones/pagos y cooperaciones por etapas**, resolviendo primero los cruces entre DTO HTTP, negocio e infraestructura.
5. **Aclarar WhatsApp** con evidencia de su flujo activo.
6. **Acordar límites de módulos** y, si se aprueba, agregar pruebas arquitectónicas para prevenir dependencias desde negocio hacia HTTP/infraestructura.

## Fuera de alcance

- No se cambió código de aplicación ni esquema de base de datos.
- No se implementó autenticación CU-CR-022. Se mantiene pendiente de definición de CU-CR-023 (usuarios/roles) y CU-CR-025 (auditoría).
- No se concluye que todas las rutas estén protegidas o desprotegidas; la revisión de seguridad requiere un análisis específico.
- No se ejecutó localmente `mvn clean verify`; el estado del build se obtuvo del workflow de GitHub Actions citado.
