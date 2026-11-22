# Guía de implementación por dominio

> Esta referencia reemplaza las plantillas antiguas que generaban clases bajo `api/business` y `api/data` y acoplaban servicios a DTOs HTTP. La migración a Maven multimódulo sigue en curso; no copies una plantilla sin verificar el dominio de referencia.

## Referencias funcionales actuales

### Comunidades
- HTTP: `api/src/main/java/com/crdev/connect_rural_api/app/community/`
- Servicio/modelos de negocio: `services/src/main/java/com/crdev/connect_rural_api/service/community/`
- Persistencia: `repositories/src/main/java/com/crdev/connect_rural_api/repository/community/`
- Entidad: `models/src/main/java/com/crdev/connect_rural_api/model/CommunityEntity.java`

### Residentes
- HTTP: `api/src/main/java/com/crdev/connect_rural_api/app/resident/`
- Servicio/modelos de negocio: `services/src/main/java/com/crdev/connect_rural_api/service/resident/`
- Persistencia: `repositories/src/main/java/com/crdev/connect_rural_api/repository/resident/`
- Entidad: `models/src/main/java/com/crdev/connect_rural_api/model/ResidentEntity.java`

## Antes de crear un dominio

1. Lee `docs/architecture.md` y los POM de los módulos.
2. Inspecciona el dominio funcional más parecido y sus pruebas.
3. Decide qué módulo debe poseer cada clase y evita duplicar clases que sigan activas en `api/business` o `api/data`.
4. Mantén los DTOs HTTP en `api`; define modelos/consultas de servicio en `services`; entidades en `models`; acceso a datos en `repositories`.
5. Para recursos comunitarios, aplica `communityKey` en todas las operaciones de lectura y mutación pertinentes.
6. Si cambia la base de datos, agrega una migración Liquibase según la skill de migración y revisa cómo se incluye en el changelog maestro.
7. Agrega pruebas y ejecuta `mvn clean verify` cuando sea posible.
8. Actualiza la matriz de migración en `docs/architecture.md`.

## Esqueleto conceptual

```text
api/app/{domain}/
  {Domain}Controller.java
  dto/                         # request/response HTTP

services/service/{domain}/
  {Domain}Service.java
  dto/                         # modelos, queries o comandos de servicio
  exception/                   # errores de dominio, si hacen falta

repositories/repository/{domain}/
  {Domain}Repository.java
  {Domain}Specs.java           # solo si la consulta lo necesita

models/model/
  {Domain}Entity.java
```

Este es un mapa conceptual, no una obligación de crear todas las clases. Usa únicamente los artefactos que necesita el dominio y confirma la estructura de paquetes real antes de implementar.
