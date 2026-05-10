# Plan: Roles y Autenticación

## Contexto

Actualmente la API no tiene autenticación. Antes de exponer el sistema a usuarios finales es necesario agregar JWT + roles. Es la feature con mayor impacto en seguridad y la que más afecta el resto del código.

---

## Roles definidos

| Rol | Alcance | Permisos |
|-----|---------|----------|
| `SUPER_ADMIN` | Global | CRUD de comunidades, asignar admins |
| `COMMUNITY_ADMIN` | Su comunidad | CRUD residentes, cooperaciones, reportes, WhatsApp |
| `RESIDENT` | Su perfil | Ver sus deudas y pagos (solo lectura) |

---

## Dependencias a agregar (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

## Cambios en base de datos

### Nueva tabla `users`

```sql
CREATE TABLE connect_rural.users (
    user_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(30) NOT NULL,  -- SUPER_ADMIN | COMMUNITY_ADMIN | RESIDENT
    community_key   UUID NULL,             -- NULL para SUPER_ADMIN
    resident_key    UUID NULL,             -- solo para rol RESIDENT
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('SUPER_ADMIN','COMMUNITY_ADMIN','RESIDENT'))
);
```

---

## Estructura de código

```
config/
  ├── SecurityConfig.java          ← filtros, CORS, rutas públicas
  ├── JwtConfig.java               ← secret, expiración
  └── JwtAuthFilter.java           ← OncePerRequestFilter

app/auth/
  ├── AuthController.java          ← POST /api/auth/login, POST /api/auth/refresh
  └── dto/
      ├── LoginRequest.java
      └── TokenResponse.java

business/auth/
  ├── AuthService.java             ← login(), refresh()
  ├── JwtService.java              ← generar y validar tokens
  └── UserRepository.java          ← interfaz (puerto BD)

data/user/
  ├── UserRepositoryImpl.java      ← implements UserRepository
  ├── UserJpaRepository.java       ← Spring Data
  └── UserEntity.java
```

---

## Endpoints de autenticación

```
POST /api/auth/login
  Body: { email, password }
  Response: { accessToken, refreshToken, expiresIn, role }

POST /api/auth/refresh
  Body: { refreshToken }
  Response: { accessToken, expiresIn }
```

---

## Seguridad por endpoint

```java
// Rutas públicas
/api/auth/**
/api/health

// Solo SUPER_ADMIN
POST   /api/communities
PUT    /api/communities/{key}
DELETE /api/communities/{key}

// SUPER_ADMIN o COMMUNITY_ADMIN (solo su comunidad)
GET    /api/{communityKey}/residents/**
POST   /api/{communityKey}/residents/**
GET    /api/{communityKey}/cooperations/**
POST   /api/{communityKey}/cooperations/**
GET    /api/{communityKey}/reports/**

// RESIDENT (solo su propio perfil)
GET    /api/{communityKey}/residents/{residentKey}  ← validar que es el propio
```

---

## Validación de alcance por comunidad

`COMMUNITY_ADMIN` solo puede operar dentro de su `communityKey`. Agregar validación en un `@Aspect` o en cada método de Service:

```java
// En cada método de Service que recibe communityKey:
SecurityContext.assertAdminOfCommunity(communityKey);
```

---

## Orden de implementación

- [ ] 1. Migración: tabla `users`
- [ ] 2. `UserEntity` + `UserJpaRepository` + `UserRepository` (interfaz) + `UserRepositoryImpl`
- [ ] 3. `JwtService` — generar, validar, extraer claims
- [ ] 4. `JwtAuthFilter`
- [ ] 5. `SecurityConfig` — rutas públicas, roles por endpoint
- [ ] 6. `AuthController` + `AuthService` — login y refresh
- [ ] 7. Validación de alcance por comunidad en métodos de Services críticos
- [ ] 8. Endpoint de gestión de usuarios (`POST /api/users`, admin only)

---

## Consideraciones

- Usar `BCryptPasswordEncoder` para los passwords — nunca almacenar en texto plano
- El `accessToken` debe expirar en 1h, el `refreshToken` en 7 días
- Agregar `spring.security.enabled=false` en el perfil `test` para no romper los tests existentes
- CORS ya está configurado en `CorsConfig.java` — revisar que Spring Security no lo sobreescriba
