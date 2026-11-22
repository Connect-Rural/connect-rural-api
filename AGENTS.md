# Guía de trabajo — Connect Rural API

Estas instrucciones describen el estado observado en `develop`. Para arquitectura y estado de migración, consulta primero [`docs/architecture.md`](docs/architecture.md); si hay discrepancias, inspecciona el código y los POM actuales antes de decidir.

## Stack y comandos

- Java 21, Spring Boot 3.5, Maven multimódulo, PostgreSQL, Liquibase.
- Compilar y probar desde la raíz:
  ```bash
  mvn clean verify
  mvn test
  mvn test -Dtest=NombreDeClase
  ```
- Ejecuta las pruebas pertinentes después de cambios. No afirmes que el build pasa si no ejecutaste el comando y verificaste el resultado.
- La configuración local usa `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD`. No leas ni publiques secretos de `.env`.

## Módulos Maven actuales

```text
models       Entidades JPA compartidas
repositories Repositorios Spring Data y consultas de persistencia
services     Servicios y modelos/consultas de negocio
api          Aplicación Spring Boot, HTTP, configuración y migraciones
```

Dependencias internas declaradas: `repositories -> models`, `services -> repositories + models`, `api -> services + models`. Comprueba los POM antes de cambiar dependencias.

## Estado arquitectónico

La migración es progresiva. Comunidades y residentes funcionan y sus controladores están en `api/app`, mientras sus servicios y modelos de negocio viven en `services/service`; persistencia y entidades están en `repositories` y `models`. Otros dominios todavía tienen implementaciones dentro de `api/business` y `api/data`.

No asumas que todo el repositorio ya sigue un patrón uniforme. Antes de crear o migrar un dominio, traza el flujo real desde el controlador hasta el servicio, repositorio y entidad. No dupliques una implementación activa ni refactorices módulos funcionales solo para que se parezcan a una estructura objetivo aún no acordada.

## Reglas para cambios

1. Mantén el alcance limitado a la tarea solicitada.
2. Respeta contratos HTTP, nombres de tablas, esquema `connect_rural` y comportamiento existente salvo que el cambio los requiera explícitamente.
3. Las modificaciones de esquema deben hacerse mediante Liquibase; revisa las migraciones existentes y la skill `connect-rural-migration`.
4. Verifica que el acceso a datos comunitarios esté correctamente acotado por `communityKey` cuando corresponda.
5. No uses `ResidentEntity` como identidad autenticable sin una decisión explícita de dominio.
6. No implementes autenticación ni autorización basándote en documentos de roles antiguos. Para CU-CR-022 hay dependencias funcionales pendientes de CU-CR-023 y CU-CR-025.
7. Actualiza la documentación e instrucciones cuando el flujo real cambie; distingue arquitectura implementada de arquitectura objetivo.
8. Nunca introduzcas secretos, credenciales o datos personales en commits, logs o respuestas.

## Configuración y persistencia

- La aplicación usa PostgreSQL y el esquema `connect_rural`.
- Liquibase administra las migraciones desde `api/src/main/resources/db/changelog/db.changelog-master.xml`.
- `spring.jpa.hibernate.ddl-auto=none` en la configuración principal; no dependas de generación automática de esquema en producción.
- El perfil de pruebas utiliza H2 y puede diferir de PostgreSQL. Si una migración depende de funciones específicas de PostgreSQL, verifica su ejecución contra PostgreSQL en un entorno seguro.

## Documentación de referencia

- [Arquitectura actual y matriz de migración](docs/architecture.md)
- [Skill de creación/migración de módulos](.agents/skills/connect-rural-module/SKILL.md)
- [Skill de migraciones Liquibase](.agents/skills/connect-rural-migration/SKILL.md)
