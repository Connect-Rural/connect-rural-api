# Plan: Portal del Residente

## Contexto

Actualmente solo los administradores interactúan con el sistema. El portal del residente permite que cada residente consulte sus deudas, historial de pagos y reciba notificaciones — todo autenticado con su propio acceso. Depende de que **Roles y Autenticación** esté implementado primero.

---

## Dependencias previas

- `plan-roles-autenticacion.md` — tabla `users`, JWT, rol `RESIDENT`

---

## Features del portal

| Feature | Descripción |
|---------|-------------|
| Login | Con email o número de teléfono + contraseña |
| Mis deudas | Lista de `financial_obligations` pendientes del residente |
| Mi historial | Todos sus pagos realizados con referencias |
| Mis multas | `fine_assignments` asignadas (si módulo de multas está activo) |
| Mi perfil | Ver datos personales (sin editar — solo el admin edita) |
| Acceso por link WhatsApp | Link mágico de acceso sin contraseña enviado por WhatsApp |

---

## Cambios en base de datos

Agregar columna a `users` para el link mágico:

```sql
ALTER TABLE connect_rural.users
    ADD COLUMN magic_token       VARCHAR(255) NULL,
    ADD COLUMN magic_token_exp   TIMESTAMP NULL;
```

---

## Nuevos endpoints (alcance del residente)

```
# Auth residente
POST /api/auth/login
POST /api/auth/magic-link          ← genera link y lo envía por WhatsApp
GET  /api/auth/magic-link/verify?token=...   ← autentica con el token

# Portal (requiere JWT con rol RESIDENT)
GET  /api/portal/me                          ← perfil del residente
GET  /api/portal/obligations                 ← deudas pendientes
GET  /api/portal/payments                    ← historial de pagos
GET  /api/portal/fines                       ← multas (si módulo activo)
```

---

## Flujo de link mágico (acceso sin contraseña)

```
1. Admin o sistema envía WhatsApp con link:
   "Hola Juan, accede a tu portal: https://app.connectrural.com/auth?token=ABC123"

2. Residente hace clic → frontend llama GET /api/auth/magic-link/verify?token=ABC123

3. API valida token y expiración → devuelve JWT con rol RESIDENT

4. Residente accede al portal sin necesidad de recordar contraseña
```

Token expira en 24 horas. Un clic lo invalida (uso único).

---

## Estructura de código

```
app/portal/
  ├── ResidentPortalController.java
  └── dto/
      ├── PortalProfileResponse.java
      ├── PortalObligationResponse.java
      └── PortalPaymentHistoryResponse.java

business/auth/
  ├── AuthService.java          ← nuevos métodos: generateMagicLink(), verifyMagicLink()
  └── UserRepository.java       ← ya existe, agregar findByMagicToken()

business/portal/
  ├── PortalService.java        ← getProfile(), getObligations(), getPaymentHistory()
```

`PortalService` inyecta `ResidentRepository`, `FinancialObligationRepository` y `ResidentPaymentRepository` directamente — no llama a otros Services.

`AuthService.generateMagicLink()` inyecta `WhatsappGateway` directamente para enviar el link — no llama a `WhatsappService`.

---

## Seguridad del portal

- Los endpoints `/api/portal/**` validan que el `residentKey` del JWT coincida con el recurso solicitado
- Un residente no puede ver datos de otro residente
- El JWT del residente incluye `residentKey` y `communityKey` en los claims

```java
// Ejemplo de validación en PortalService:
UUID tokenResidentKey = SecurityContext.getResidentKey();
if (!tokenResidentKey.equals(UUID.fromString(residentKey))) {
    throw new AccessDeniedException("No autorizado");
}
```

---

## Métodos de pago en línea (extensión futura)

Si se quiere que el residente pague desde el portal:

- Integrar pasarela (Stripe, PayU, SINPE Móvil según región)
- `POST /api/portal/obligations/{obligationKey}/pay` con redirect a pasarela
- Webhook de confirmación → llama `CooperationService.markAsPaid()` automáticamente
- Guardar `method=SINPE|STRIPE` y `reference` real en `resident_payments`

---

## Orden de implementación

- [ ] 0. Prerequisito: `plan-roles-autenticacion.md` completo
- [ ] 1. Migración: columnas `magic_token` y `magic_token_exp` en `users`
- [ ] 2. DTOs de respuesta del portal
- [ ] 3. `PortalService.getProfile()`
- [ ] 4. `PortalService.getObligations()`
- [ ] 5. `PortalService.getPaymentHistory()`
- [ ] 6. `ResidentPortalController`
- [ ] 7. `AuthService.generateMagicLink()` + envío por WhatsApp
- [ ] 8. `AuthService.verifyMagicLink()`
- [ ] 9. (Opcional) Integración con pasarela de pagos
