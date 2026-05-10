# Documentación — connect-rural-api

- [Arquitectura](architecture.md) — capas, reglas de dependencia, naming conventions
- [Plan de migración de arquitectura](plan-migracion-arquitectura.md) — pasos para migrar el código existente

## Features

Planes de implementación ordenados por prioridad. La lógica de la secuencia: primero tener algo usable por el admin, luego protegerlo con auth, luego expandir hacia los residentes y la madurez operativa.

---

## Fase 0 — Prerequisito

| # | Feature | Archivo | Esfuerzo |
|---|---------|---------|----------|
| 0 | Migración de arquitectura | [plan-migracion-arquitectura.md](plan-migracion-arquitectura.md) | Medio |

---

## Fase 1 — Administración básica usable

El admin puede operar la comunidad de punta a punta sin autenticación (entorno interno/controlado). Al final de esta fase el ciclo financiero está completo: ingresos, egresos y reportes.

| # | Feature | Archivo | Esfuerzo |
|---|---------|---------|----------|
| 1 | Dashboard por comunidad | [plan-dashboard.md](plan-dashboard.md) | Bajo |
| 2 | Cooperaciones recurrentes | [plan-cooperaciones-recurrentes.md](plan-cooperaciones-recurrentes.md) | Medio |
| 3 | Módulo de multas | [plan-multas.md](plan-multas.md) | Medio |
| 4 | Egresos y balance financiero | [plan-modulo-contable.md](plan-modulo-contable.md) | Medio |
| 5 | Reportes y exportación PDF/Excel | [plan-reportes-exportacion.md](plan-reportes-exportacion.md) | Bajo |
| 6 | Notificaciones WhatsApp automáticas | [plan-notificaciones-whatsapp.md](plan-notificaciones-whatsapp.md) | Bajo |

---

## Fase 2 — Autenticación y roles

Una vez que hay algo que vale la pena proteger.

| # | Feature | Archivo | Esfuerzo |
|---|---------|---------|----------|
| 7 | Roles y autenticación | [plan-roles-autenticacion.md](plan-roles-autenticacion.md) | Alto |

---

## Fase 3 — Expansión de módulos

Requiere auth para control de acceso por comunidad y por residente.

| # | Feature | Archivo | Esfuerzo |
|---|---------|---------|----------|
| 8 | Portal del residente | [plan-portal-residente.md](plan-portal-residente.md) | Alto |
| 9 | Trabajos comunitarios | [plan-trabajos-comunitarios.md](plan-trabajos-comunitarios.md) | Alto |
| 10 | Solicitudes de mantenimiento | [plan-solicitudes-mantenimiento.md](plan-solicitudes-mantenimiento.md) | Medio |

---

## Fase 4 — Madurez operativa

| # | Feature | Archivo | Esfuerzo |
|---|---------|---------|----------|
| 11 | Presupuesto por categoría | [plan-modulo-contable.md](plan-modulo-contable.md) | Medio |
| 12 | Gestión de asambleas | [plan-gestion-asambleas.md](plan-gestion-asambleas.md) | Alto |
| 13 | Módulo de documentos y plantillas | [plan-modulo-documentos.md](plan-modulo-documentos.md) | Alto |

---

## Dependencias entre features

```
Migración de arquitectura (Fase 0)
  └── todo lo demás

Dashboard (Fase 1)
  └── no tiene dependencias — solo consulta datos existentes

Cooperaciones recurrentes (Fase 1)
  └── extiende el módulo de cooperaciones existente

Módulo de multas (Fase 1)
  └── se integra con financial_obligations (ya existe)
  └── Trabajos comunitarios (saldar multa con trabajo)

Egresos + balance (Fase 1)
  └── depende de multas para incluir todos los ingresos en el balance

Presupuesto (Fase 4)
  └── extiende el módulo contable — requiere egresos ya implementados
  └── Solicitudes de mantenimiento (registrar gasto por reparación)

Reportes PDF (Fase 1)
  └── depende de módulo contable para reportes financieros completos
  └── Módulo de documentos (comparte generador iText7)

Notificaciones WhatsApp (Fase 1)
  └── usa infraestructura WhatsApp ya existente

Roles y autenticación (Fase 2)
  └── Portal del residente (necesita JWT con rol RESIDENT)
  └── Solicitudes de mantenimiento (residente reporta desde portal)

Trabajos comunitarios (Fase 3)
  └── Solicitudes de mantenimiento (resolver con trabajo comunitario)

Gestión de asambleas (Fase 4)
  └── Módulo de documentos (acta generada al cerrar asamblea)
```
