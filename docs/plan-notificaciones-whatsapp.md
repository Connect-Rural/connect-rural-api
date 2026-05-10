# Plan: Notificaciones WhatsApp Automáticas

## Contexto

La infraestructura de WhatsApp ya existe (`WhatsappService`, `WhatsappGateway`). Esta feature agrega los triggers automáticos para notificar a residentes sobre eventos de pago sin intervención manual.

---

## Notificaciones a implementar

| Trigger | Momento | Destinatario |
|---------|---------|--------------|
| Recordatorio de pago | N días antes del `dueDate` | Residente con pago pendiente |
| Confirmación de pago | Al marcar como pagado | Residente pagado |
| Aviso de recargo activado | Cuando `dueDate` pasa y sigue pendiente | Residente moroso |
| Nuevo residente asignado | Al crear/actualizar asignación | Residente recién incluido |
| Comunicado masivo | Manual desde el admin | Todos los residentes de la comunidad |

---

## Cambios requeridos

### Base de datos — nueva tabla `notification_log`

```sql
CREATE TABLE connect_rural.notification_log (
    log_key         UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    resident_key    UUID NOT NULL,
    origin_type     VARCHAR(50) NOT NULL,  -- COOPERATION, FINE, MANUAL
    origin_id       UUID NULL,
    event_type      VARCHAR(50) NOT NULL,  -- PAYMENT_REMINDER, PAYMENT_CONFIRMED, etc.
    status          VARCHAR(20) NOT NULL DEFAULT 'SENT',  -- SENT | FAILED
    sent_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Sirve para evitar duplicados y auditar envíos.

### Scheduler — `@Scheduled`

Dos jobs en `business/whatsapp/scheduled/`:

**`PaymentReminderJob`** — corre diariamente:
```
Para cada comunidad con whatsappAppKey configurado:
  Buscar obligations con status=PENDING y dueDate = hoy + N días
  Por cada una: whatsappService.sendPaymentReminder(...) + registrar en notification_log
```

**`LatePaymentNotificationJob`** — corre diariamente:
```
Buscar obligations con status=PENDING y dueDate < hoy
Filtrar las que no tienen log reciente (evitar spam)
Notificar y registrar
```

### Configuración por comunidad

Agregar columnas a `communities`:
```sql
notification_days_before  INT DEFAULT 3   -- días antes del vencimiento para recordatorio
notifications_enabled     BOOLEAN DEFAULT FALSE
```

### Nuevos métodos en WhatsappService

- `sendPaymentReminder(UUID communityKey, UUID residentKey, UUID obligationKey)` — arma el mensaje y llama a `whatsappGateway.send()`
- `sendMassMessage(UUID communityKey, String message)` — endpoint manual `POST /api/{communityKey}/notify`
- `sendPaymentConfirmation(UUID communityKey, UUID residentKey, UUID obligationKey)` — llamado desde `CooperationService.markAsPaid()` si la comunidad tiene notificaciones activas

`WhatsappService` inyecta `CommunityRepository` y `ResidentRepository` para obtener los datos necesarios — no llama a otros Services.

### Plantillas de mensaje

Definir en constantes o en BD. Ejemplo:
```
"Hola {nombre}, te recordamos que tu cuota '{cooperacion}' 
vence el {fecha}. Monto: ₡{monto}. — {comunidad}"
```

---

## Estructura de código

```
business/whatsapp/
  ├── WhatsappService.java             ← nuevos métodos: sendPaymentReminder, sendMassMessage, sendPaymentConfirmation
  ├── WhatsappGateway.java             ← interfaz (puerto HTTP)
  └── scheduled/
      ├── PaymentReminderJob.java
      └── LatePaymentNotificationJob.java

business/notificationlog/
  ├── NotificationLogService.java
  └── NotificationLogRepository.java  ← interfaz (puerto BD)

data/notificationlog/
  ├── NotificationLogRepositoryImpl.java
  ├── NotificationLogJpaRepository.java
  └── NotificationLogEntity.java

app/community/
  └── dto/
      └── SendMassMessageRequest.java  ← nuevo DTO para endpoint masivo
```

---

## Orden de implementación

- [ ] 1. Migración: tabla `notification_log` + columnas en `communities`
- [ ] 2. `NotificationLogEntity` + `NotificationLogJpaRepository` + `NotificationLogRepository` (interfaz) + `NotificationLogRepositoryImpl`
- [ ] 3. `NotificationLogService`
- [ ] 4. `WhatsappService.sendPaymentReminder()`
- [ ] 5. `WhatsappService.sendPaymentConfirmation()` — integrar en `CooperationService.markAsPaid()`
- [ ] 6. `PaymentReminderJob` con `@Scheduled`
- [ ] 7. `LatePaymentNotificationJob`
- [ ] 8. `WhatsappService.sendMassMessage()` + endpoint en `CommunityController`
- [ ] 9. Columnas de configuración por comunidad

---

## Consideraciones

- Validar siempre que `community.whatsappAppKey != null` y `resident.phoneNumber != null` antes de enviar
- El job de recordatorio debe respetar `notifications_enabled` por comunidad
- Usar `notification_log` para no enviar el mismo evento dos veces al mismo residente en el mismo día
