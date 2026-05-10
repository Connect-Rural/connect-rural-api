# Plan: Dashboard por Comunidad

## Contexto

Un endpoint agregado que devuelve los KPIs principales de una comunidad. Sin modelo nuevo — es pura consulta sobre datos existentes.

---

## Endpoint

```
GET /api/{communityKey}/dashboard
```

---

## Respuesta

```json
{
  "communityKey": "...",
  "communityName": "...",
  "generatedAt": "2026-04-01T00:00:00",

  "residents": {
    "total": 120,
    "active": 115,
    "inactive": 5
  },

  "cooperations": {
    "total": 8,
    "open": 3,
    "closed": 5
  },

  "payments": {
    "totalExpected": 15000.00,
    "totalCollected": 9500.00,
    "totalPending": 5500.00,
    "collectionRate": 63.3
  },

  "delinquency": {
    "residentsWithOverduePayments": 12,
    "totalOverdueAmount": 3200.00
  },

  "openCooperations": [
    {
      "cooperationKey": "...",
      "name": "Cuota Abril 2026",
      "dueDate": "2026-04-30",
      "progress": 72.5,
      "totalAssigned": 40,
      "totalPaid": 29,
      "totalPending": 11
    }
  ]
}
```

---

## Estructura de código

```
app/community/
  └── dto/
      └── CommunityDashboardResponse.java   ← nuevo DTO

business/community/
  └── CommunityService.java                 ← nuevo método getDashboard(UUID communityKey)
```

`CommunityService.getDashboard()` inyecta:
- `CooperationRepository` — conteos por status
- `ResidentRepository` — conteos por activo/inactivo
- `FinancialObligationRepository` — totales y morosidad

No llama a otros Services — solo Repositories.

Agregar endpoint en `CommunityController`:
```java
GET /api/communities/{communityKey}/dashboard
```

---

## Queries necesarias

Todas sobre datos existentes, sin joins complejos:

| Dato | Fuente |
|------|--------|
| Residentes total/activos | `ResidentRepository.countByCommunityKey(...)` |
| Cooperaciones open/closed | `CooperationRepository.countByCommunityKeyAndStatus(...)` |
| Total esperado | `SUM(financial_obligations.amount_due)` filtrado por comunidad |
| Total cobrado | `SUM(resident_payments.amount)` via allocations de la comunidad |
| Residentes morosos | `COUNT DISTINCT` de obligations PENDING con dueDate < hoy |
| Cooperaciones abiertas | Lista con progreso — reutiliza lógica de `CooperationRepository` |

Agregar en los Repository interfaces los métodos de conteo necesarios o usar queries `@Query` en los JpaRepositories correspondientes.

---

## Orden de implementación

- [ ] 1. `CommunityDashboardResponse`
- [ ] 2. Métodos de conteo en `CooperationRepository` y `ResidentRepository` (interfaces)
- [ ] 3. Query agregada en `FinancialObligationRepository` para totales por comunidad
- [ ] 4. `CommunityService.getDashboard()`
- [ ] 5. Endpoint en `CommunityController`

---

## Consideraciones

- Agregar `@Cacheable` si las consultas se vuelven lentas con muchos datos
- El campo `generatedAt` ayuda al frontend a saber cuándo fue la última actualización
- Separar `openCooperations` como lista paginada si la comunidad tiene muchas abiertas
